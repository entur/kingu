package org.entur.kingu.route.export;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.entur.kingu.KingRouteBuilderIntegrationTestBase;
import org.entur.kingu.TestApp;
import org.entur.kingu.exporter.async.NetexExporter;
import org.entur.kingu.model.job.ExportJob;
import org.entur.kingu.model.job.JobStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.test.junit5.TestSupport.createExchangeWithBody;
import static org.entur.kingu.Constants.XML_FILE_PATH;
import static org.entur.kingu.Constants.ZIP_FILE_PATH;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TestApp.class)
class NetexExportRouteTest extends KingRouteBuilderIntegrationTestBase {


    @EndpointInject("mock:netexExport")
    protected MockEndpoint  mockNetexExport;

    @EndpointInject("mock:netexValidator")
    protected MockEndpoint netexValidator;

    @EndpointInject("mock:zipNetexExport")
    protected MockEndpoint mockZipNetexExport;

    @EndpointInject("mock:uploadToGcsBucket")
    protected MockEndpoint mockUploadToGcsBucket;

    @Produce("{{pubsub.kingu.outbound.topic.netex.export}}")
    protected ProducerTemplate outboundExportTemplate;

    @Produce("direct:netexExport")
    protected ProducerTemplate netexExportTemplate;

    @Produce("direct:validateNetexExport")
    protected ProducerTemplate validateNetexExportTemplate;

    @Produce("direct:zipNetexExport")
    protected ProducerTemplate zipNetexExportTemplate;

    @Produce("direct:cleanUpLocalDirectory")
    protected ProducerTemplate cleanUpLocalDirectoryTemplate;

    @Produce("direct:uploadToGcsBucket")
    protected ProducerTemplate uploadToGcsBucketTemplate;


    @MockBean
    protected NetexExporter netexExporter;

    @Value("${pubsub.kingu.outbound.topic.netex.export}")
    private String outGoingNetexExport;

    @Test
    void incomingMessageShouldBeParsedAndExportJobIsCreated() throws Exception {

        AdviceWith.adviceWith(context,"netex-export-job-builder-route",
                a -> a.weaveByToUri("direct:netexExport").replace().to("mock:netexExport"));


        Exchange inputExchange = createExchangeWithBody(context,generateMessage());


        context.start();

        outboundExportTemplate.send(inputExchange);

        mockNetexExport.expectedMessageCount(1);
        mockNetexExport.assertIsSatisfied();

        Assertions.assertEquals(1,mockNetexExport.getExchanges().size());

        final Exchange exchange = mockNetexExport.assertExchangeReceived(0);
        final ExportJob exportJob = exchange.getIn().getBody(ExportJob.class);

        Assertions.assertEquals("15_More og Romsdal",exportJob.getExportParams().getName());
        Assertions.assertEquals(JobStatus.PROCESSING,exportJob.getStatus());

    }

    @Test
    void exportJobProcessedSuccessfully() throws Exception {
        AdviceWith.adviceWith(context,"netex-export-route",
                a -> a.weaveByToUri("direct:validateNetexExport").replace().to("mock:netexValidator"));


        Exchange inputExchange = createExchangeWithBody(context,createExportJob(JobStatus.PROCESSING));

        when(netexExporter.process(inputExchange)).thenReturn(createExportJob(JobStatus.FINISHED));

        context.start();

        netexExportTemplate.send(inputExchange);

        netexValidator.assertIsSatisfied();

        Assertions.assertEquals(1,netexValidator.getExchanges().size());

        final Exchange exchange = netexValidator.assertExchangeReceived(0);
        final ExportJob exportJob = exchange.getIn().getBody(ExportJob.class);

        Assertions.assertEquals(JobStatus.FINISHED,exportJob.getStatus());

        final long actualDurationInMinutes = Duration.between(exportJob.getStarted(), exportJob.getFinished()).getSeconds()/60;

        Assertions.assertEquals(10L,actualDurationInMinutes);


    }

    @Test
    void netexValidatorTest() throws Exception{

        AdviceWith.adviceWith(context,"netex-reference-validator-route",
                a -> a.weaveByToUri("direct:zipNetexExport").replace().to("mock:zipNetexExport"));

        Exchange inputExchange = createExchangeWithBody(context,createExportJob(JobStatus.FINISHED));

        context.start();
        validateNetexExportTemplate.send(inputExchange);

        mockZipNetexExport.assertIsSatisfied();

        Assertions.assertEquals(1, mockZipNetexExport.getExchanges().size());
    }

    @Test
    @Disabled("Ignoring test unable to creat local zip file")
    void netexExportZipTest() throws Exception {
        AdviceWith.adviceWith(context,"zip-netex-export-route",
                a -> a.weaveByToUri("direct:uploadToGcsBucket").replace().to("mock:uploadToGcsBucket"));

        File localExportZipFile = new File("/tmp/tiamat-export.zip");

        Exchange inputExchange = createExchangeWithBody(context,createExportJob(JobStatus.FINISHED));

        context.start();
        zipNetexExportTemplate.send(inputExchange);
        mockUploadToGcsBucket.assertIsSatisfied();
        Assertions.assertEquals(1, mockUploadToGcsBucket.getExchanges().size());

       Assertions.assertTrue(localExportZipFile.delete());
    }

    @Test
    void cleanUpLocalFilesTest() {
        final String tmpXmlFilePath = "/tmp/tempFileXml.xml";
        createTempFiles(tmpXmlFilePath);
        final String tmpZipFilePath = "/tmp/tempFileZip.zip";
        createTempFiles(tmpZipFilePath);

        File tmpXmlFile = new File(tmpXmlFilePath);
        File tmpZipFile = new File(tmpZipFilePath);

        Map headers = new HashMap<String,String>();
        headers.put(XML_FILE_PATH, tmpXmlFilePath);
        headers.put(ZIP_FILE_PATH, tmpZipFilePath);

        context.start();
        cleanUpLocalDirectoryTemplate.sendBodyAndHeaders("", headers);

        Assertions.assertFalse(tmpXmlFile.exists());
        Assertions.assertFalse(tmpZipFile.exists());

    }

    private void createTempFiles(String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write("fileContent".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateMessage(){
        var msg = """
                {
                	"name": "15_More og Romsdal",
                	"tariffZoneExportMode": "RELEVANT",
                	"fareZoneExportMode": "RELEVANT",
                	"groupOfStopPlacesExportMode": "RELEVANT",
                	"groupOfTariffZonesExportMode": "RELEVANT",
                	"serviceFrameExportMode": "NONE",
                	"topographicPlaceExportMode": "RELEVANT",
                	"municipalityReferences": null,
                	"countyReferences": ["KVE:TopographicPlace:15"],
                	"countryReferences": null,
                	"stopPlaceSearch": {
                		"query": null,
                		"stopTypeEnumerations": null,
                		"submode": null,
                		"netexIdList": null,
                		"allVersions": false,
                		"versionValidity": "CURRENT",
                		"withoutLocationOnly": false,
                		"withoutQuaysOnly": false,
                		"withDuplicatedQuayImportedIds": false,
                		"withNearbySimilarDuplicates": false,
                		"hasParking": false,
                		"version": null,
                		"tags": null,
                		"withTags": false,
                		"pointInTime": null
                	}
                }
                """;
        return msg;
    }

}
