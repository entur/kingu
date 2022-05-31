/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.entur.kingu.exporter.async;


import com.google.common.io.ByteStreams;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.exporter.StreamingPublicationDelivery;
import org.entur.kingu.model.job.ExportJob;
import org.entur.kingu.model.job.JobStatus;
import org.entur.kingu.netex.validation.NetexXmlReferenceValidator;
import org.entur.kingu.service.BlobStoreService;
import org.entur.kingu.time.ExportTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.entur.kingu.Constants.EXPORT_JOB_NAME;
import static org.entur.kingu.Constants.EXPORT_LOCATION;


public class ExportJobWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ExportJobWorker.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSS");

    /**
     * Ignore paging for async export, to not let the default value interfer.
     */
    public static final boolean IGNORE_PAGING = true;
    private final BlobStoreService blobStoreService;
    private final ExportParams exportParams;
    private final ExportTimeZone exportTimeZone;
    private final StreamingPublicationDelivery streamingPublicationDelivery;
    private final String localExportPath;
    private final NetexXmlReferenceValidator netexXmlReferenceValidator;
    private final CamelContext camelContext;
    private final String outGoingNetexExport;
    private final String breadcrumbId;


    public ExportJobWorker(BlobStoreService blobStoreService,
                           ExportParams exportParams,
                           ExportTimeZone exportTimeZone,
                           StreamingPublicationDelivery streamingPublicationDelivery,
                           String localExportPath,
                           NetexXmlReferenceValidator netexXmlReferenceValidator,
                           CamelContext camelContext,
                           String outGoingNetexExport,
                           String breadcrumbId) {
        this.blobStoreService =blobStoreService;
        this.exportParams = exportParams;
        this.exportTimeZone = exportTimeZone;
        this.streamingPublicationDelivery = streamingPublicationDelivery;
        this.localExportPath = localExportPath;
        this.netexXmlReferenceValidator = netexXmlReferenceValidator;
        this.camelContext =camelContext;
        this.outGoingNetexExport = outGoingNetexExport;
        this.breadcrumbId =breadcrumbId;
    }


    public void run() {
        MDC.put("camel.breadcrumbId",breadcrumbId );
        final ExportJob exportJob = createExportJob();
        logger.info("Started export job: {}", exportJob);
        final File localExportZipFile = new File(localExportPath + File.separator + exportJob.getFileName());
        var fileNameWithoutExtension= createFileNameWithoutExtension(exportJob.getStarted(),exportParams.getName());
        File localExportXmlFile = new File(localExportPath + File.separator + fileNameWithoutExtension + ".xml");
        try {

            exportToLocalXmlFile(exportJob,localExportXmlFile);

            netexXmlReferenceValidator.validateNetexReferences(localExportXmlFile);

            var newFile = localExportZipFile.createNewFile();

            if (newFile) {
                logger.debug("Created new localZipFile");
            }

            exportToLocalZipFile(fileNameWithoutExtension,localExportZipFile, localExportXmlFile);

            uploadToGcp(exportJob,localExportZipFile);

            exportJob.setStatus(JobStatus.FINISHED);
            exportJob.setFinished(Instant.now());
            logger.info("Sending finished job to jms");
            sendJMS(exportJob);
            logger.warn("Duration(secs): {},Export job done: {} ",Duration.between(exportJob.getStarted(),exportJob.getFinished()).getSeconds(),exportJob);

        } catch (Exception e) {
            exportJob.setStatus(JobStatus.FAILED);
            String message = "Error executing export job " + exportJob.getId() + ". " + e.getClass().getSimpleName() + " - " + e.getMessage();
            logger.error("{}.\nExport job was {}", message, exportJob, e);
            exportJob.setMessage(message);
            if (e instanceof InterruptedException) {
                logger.info("The export job was interrupted: {}", exportJob);
                Thread.currentThread().interrupt();
            }
        } finally {
            logger.info("Removing local files: {},{}", localExportZipFile, localExportXmlFile);

            try {
                cleanUp(localExportZipFile.toPath());
                cleanUp(localExportXmlFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            MDC.remove("camel.breadcrumbId");

        }
    }

    private ExportJob createExportJob() {
        ExportJob exportJob = new ExportJob(JobStatus.PROCESSING);
        final Instant now = Instant.now();
        exportJob.setId(Long.valueOf(now.atZone(exportTimeZone.getDefaultTimeZoneId()).format(DATE_TIME_FORMATTER)));
        exportJob.setStarted(now);
        exportJob.setExportParams(exportParams);
        exportJob.setSubFolder(generateSubFolderName());
        String fileNameWithoutExtension = createFileNameWithoutExtension(exportJob.getStarted(),exportParams.getName());
        exportJob.setFileName(fileNameWithoutExtension + ".zip");
        exportJob.setJobUrl("export" + "/" + exportJob.getId());
        return exportJob;
    }
    private String generateSubFolderName() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        return localDateTime.getYear() + "-" + String.format("%02d", localDateTime.getMonthValue());
    }

    private String createFileNameWithoutExtension(Instant started, String name) {
        final String fileName;
        var fileNameSuffix = started.atZone(exportTimeZone.getDefaultTimeZoneId()).format(DATE_TIME_FORMATTER);
        if (name != null && !name.isEmpty()) {
            fileName = "tiamat-export-"+name+ "-" + fileNameSuffix;
        } else {
            fileName = "tiamat-export-" + fileNameSuffix;
        }
        return fileName;
    }

    private void exportToLocalXmlFile(ExportJob exportJob,File localExportXmlFile) throws InterruptedException, IOException, XMLStreamException, SAXException, JAXBException {
        logger.info("Start streaming publication delivery to local file {}", localExportXmlFile);
        FileOutputStream fileOutputStream = new FileOutputStream(localExportXmlFile);
        streamingPublicationDelivery.stream(exportJob.getExportParams(), fileOutputStream, IGNORE_PAGING);
    }

    private void uploadToGcp(ExportJob exportJob,File localExportFile) throws FileNotFoundException {
        logger.info("Uploading to gcp: {} in folder: {}", exportJob.getFileName(), exportJob.getSubFolder());
        FileInputStream fileInputStream = new FileInputStream(localExportFile);
        blobStoreService.upload(exportJob.getSubFolder() + "/" + exportJob.getFileName(), fileInputStream);
    }

    private void exportToLocalZipFile(String fileNameWithoutExtension,File localZipFile, File localExportZipFile) throws IOException, InterruptedException, JAXBException, XMLStreamException, SAXException {
        logger.info("Adding {} to zip file: {}", localExportZipFile, localZipFile);

        final FileOutputStream fileOutputStream = new FileOutputStream(localZipFile);
        final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        try {
            zipOutputStream.putNextEntry(new ZipEntry(fileNameWithoutExtension + ".xml"));

            InputStream fileInputStream = new FileInputStream(localExportZipFile);
            ByteStreams.copy(fileInputStream,zipOutputStream);
            zipOutputStream.closeEntry();
            logger.info("Written to disk {}", localZipFile);
        } finally {
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                logger.error(String.format("Could not close zipoutput stream for file: %s", localZipFile), e);
            }
        }
    }

    private void sendJMS(ExportJob exportJob) {
        try(ProducerTemplate template = camelContext.createProducerTemplate()){
            var url = exportJob.getSubFolder() + "/" + exportJob.getFileName();
            var body = exportJob.getExportParams().toString();
            logger.info("send to JMS: {}", body);
            HashMap<String, Object> headers = new HashMap<>();
            headers.put(EXPORT_JOB_NAME,exportJob.getExportParams().getName());
            headers.put(EXPORT_LOCATION, url);
            template.sendBodyAndHeaders(outGoingNetexExport,body,headers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanUp(Path path) throws IOException {
        Files.delete(path);
    }
}
