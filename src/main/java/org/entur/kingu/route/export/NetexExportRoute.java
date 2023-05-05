package org.entur.kingu.route.export;

import io.micrometer.core.instrument.Timer;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.processor.ThrottlerRejectedExecutionException;
import org.entur.kingu.exporter.async.NetexExporter;
import org.entur.kingu.model.job.ExportJob;
import org.entur.kingu.model.job.JobStatus;
import org.entur.kingu.netex.validation.NetexReferenceValidatorException;
import org.entur.kingu.netex.validation.NetexXmlReferenceValidator;
import org.entur.kingu.route.BaseRouteBuilder;
import org.entur.kingu.service.BlobStoreService;
import org.entur.kingu.service.NetexExportJobBuilderException;
import org.entur.kingu.service.NetexJobBuilder;
import org.entur.kingu.service.PrometheusMetricsService;
import org.entur.kingu.utils.ZipFileUtils;
import org.hibernate.TransactionException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.entur.kingu.Constants.CAMEL_BREADCRUMB_ID;
import static org.entur.kingu.Constants.EXPORT_JOB_NAME;
import static org.entur.kingu.Constants.EXPORT_LOCATION;
import static org.entur.kingu.Constants.NETEX_EXPORT_NAME;
import static org.entur.kingu.Constants.NETEX_EXPORT_STATUS_HEADER;
import static org.entur.kingu.Constants.NETEX_EXPORT_STATUS_VALUE;
import static org.entur.kingu.Constants.XML_FILE_PATH;
import static org.entur.kingu.Constants.ZIP_FILE_PATH;

@Component
public class NetexExportRoute extends BaseRouteBuilder {

    @Value("${pubsub.kingu.inbound.subscription.netex.export}")
    private String netexExportSubscription;

    @Value("${pubsub.kingu.outbound.topic.netex.export}")
    private String netexExportTopic;

    @Value("${async.export.path:/deployments/data/}")
    String localExportPath;

    @Autowired
    NetexJobBuilder netexJobBuilder;

    @Autowired
    NetexExporter netexExporter;

    @Autowired
    NetexXmlReferenceValidator netexXmlReferenceValidator;

    @Autowired
    BlobStoreService blobStoreService;

    @Autowired
    PrometheusMetricsService metricsService;

    @Override
    public void configure() throws Exception {
        super.configure();

        onException(ThrottlerRejectedExecutionException.class).continued(true)
                .log("There are more than one inComing messages");

        onException(NetexReferenceValidatorException.class)
                .handled(true)
                .log(LoggingLevel.WARN, "Netex export validation failed ");

        onException(TransactionException.class)
                .stop()
                .log(LoggingLevel.ERROR,"Error while export ${body}");

        onException(NetexExportJobBuilderException.class)
                .stop()
                .log(LoggingLevel.ERROR,"Error while export ${body}");


        //todo better not daisychain move in to first route

        from(netexExportSubscription)
                .throttle(3).rejectExecution(true)
                .log(LoggingLevel.INFO, "Starting Tiamat exports-1: ${body}")
                .to("direct:netexJobBuilder")
                .routeId("from-tiamat-export-queue-process");

        from("direct:netexJobBuilder")
                .log(LoggingLevel.INFO,"Creating new export job.")
                .setHeader(Exchange.FILE_PARENT,constant(localExportPath))
                .bean(netexJobBuilder,"createExportJob")
                .log(LoggingLevel.INFO,"export jobs: ${body}")
                .to("direct:netexExport")
                .routeId("netex-export-job-builder-route");


        from("direct:netexExport")
                .log(LoggingLevel.INFO,"Start netex export: ${body}")
                .bean(netexExporter,"process")
                .to("direct:validateNetexExport")
                .routeId("netex-export-route");

        from("direct:validateNetexExport")
                .log(LoggingLevel.INFO,"Validating netex export: ${body}")
                .process(e -> {
                    MDC.put(CAMEL_BREADCRUMB_ID, (String) e.getIn().getHeader(Exchange.BREADCRUMB_ID));
                    final ExportJob exportJob = e.getIn().getBody(ExportJob.class);
                    MDC.put(NETEX_EXPORT_NAME, exportJob.getExportParams().getName());
                    netexXmlReferenceValidator.validateNetexReferences(new File(exportJob.getLocalExportXmlFile()));
                })
                .to("direct:zipNetexExport")
                .routeId("netex-reference-validator-route");

        from("direct:zipNetexExport")
                .log(LoggingLevel.INFO,"zip local export file: ${body}")
                .process(e -> {
                    MDC.put(CAMEL_BREADCRUMB_ID, (String) e.getIn().getHeader(Exchange.BREADCRUMB_ID));
                    final ExportJob exportJob = e.getIn().getBody(ExportJob.class);
                    MDC.put(NETEX_EXPORT_NAME, exportJob.getExportParams().getName());
                    ZipFileUtils.exportToLocalZipFile(exportJob.getLocalExportZipFile(),exportJob.getLocalExportXmlFile());
                    MDC.remove(CAMEL_BREADCRUMB_ID);
                    MDC.remove(NETEX_EXPORT_NAME);
                })
                .to("direct:uploadToGcsBucket")
                .routeId("zip-netex-export-route");

        from("direct:uploadToGcsBucket")
                .log(LoggingLevel.INFO,"uploading to gcs bucket netex export: ${body}")

                .process(e -> {
                    MDC.put(CAMEL_BREADCRUMB_ID, (String) e.getIn().getHeader(Exchange.BREADCRUMB_ID));
                    final ExportJob exportJob = e.getIn().getBody(ExportJob.class);
                    MDC.put(NETEX_EXPORT_NAME, exportJob.getExportParams().getName());
                    FileInputStream fileInputStream = new FileInputStream(exportJob.getLocalExportZipFile());
                    final String fileName = exportJob.getSubFolder() + File.separator + exportJob.getFileName() + ".zip";
                    blobStoreService.upload(fileName, fileInputStream);
                    exportJob.setStatus(JobStatus.FINISHED);
                    exportJob.setFinished(Instant.now());
                    final long duration = Duration.between(exportJob.getStarted(), exportJob.getFinished()).getSeconds();
                    log.info("Duration(secs): {},Export job done: {} ", duration,exportJob);
                    final Timer timer = metricsService.exportTimer(exportJob.getExportParams().getName());
                    timer.record(duration, TimeUnit.SECONDS);
                    metricsService.exportCounter();
                    e.getIn().setHeader(EXPORT_JOB_NAME,exportJob.getExportParams().getName());
                    e.getIn().setHeader(EXPORT_LOCATION, fileName);
                    e.getIn().setHeader(ZIP_FILE_PATH, exportJob.getLocalExportZipFile());
                    e.getIn().setHeader(XML_FILE_PATH, exportJob.getLocalExportXmlFile());
                    e.getIn().setHeader(NETEX_EXPORT_STATUS_HEADER,NETEX_EXPORT_STATUS_VALUE);

                    MDC.remove(CAMEL_BREADCRUMB_ID);
                    MDC.remove(NETEX_EXPORT_NAME);
                })
                .convertBodyTo(String.class)
                .to(netexExportTopic)
                .setHeader(Exchange.FILE_PARENT, constant(localExportPath))
                .to("direct:cleanUpLocalDirectory")
                .routeId("upload-to-gcs-bucket-route");


        from("direct:cleanUpLocalDirectory")
                .log(LoggingLevel.INFO, getClass().getName(), "Deleting local files ${header." + ZIP_FILE_PATH + "} and ${header." + XML_FILE_PATH + "}")
                .process(exchange -> {
                    Path zipFilePath = Paths.get(exchange.getIn().getHeader(ZIP_FILE_PATH, String.class));
                    Path xmlFilePath = Paths.get(exchange.getIn().getHeader(XML_FILE_PATH, String.class));
                    try {
                        Files.delete(zipFilePath);
                        Files.delete(xmlFilePath);
                    } catch (IOException e) {
                        log.info("Error delete temporary files: {}", e.getMessage());
                    }
                })
                .log(LoggingLevel.INFO, getClass().getName(), "Local files ${header." + ZIP_FILE_PATH + "} and ${header." + XML_FILE_PATH + "} cleanup done.")
                .routeId("cleanup-local-dir");

    }


}
