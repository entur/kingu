package org.entur.kingu.route.export;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.entur.kingu.KingRouteBuilderIntegrationTestBase;
import org.entur.kingu.TestApp;
import org.entur.kingu.model.job.ExportJob;
import org.entur.kingu.model.job.JobStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.entur.kingu.Constants.NETEX_EXPORT_STATUS_HEADER;
import static org.entur.kingu.Constants.NETEX_EXPORT_STATUS_VALUE;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TestApp.class)
class NetexExportRouteUploadToGcsTest extends KingRouteBuilderIntegrationTestBase {


    @EndpointInject("mock:pubSubTopic")
    protected MockEndpoint mockPubSubTopic;

    @Produce("direct:uploadToGcsBucket")
    protected ProducerTemplate sentToPubSub;

    @Value("${pubsub.kingu.outbound.topic.netex.export}")
    private String outGoingNetexExport;

    @Test
    void uploadToGcsBucketTest() throws Exception {

        AdviceWith.adviceWith(context, "upload-to-gcs-bucket-route",
                a -> {
                    a.weaveByToUri(outGoingNetexExport).replace().to("mock:pubSubTopic");
                    a.weaveByToUri("direct:cleanUpLocalDirectory").replace().to("mock:cleanUpLocalDirectory");
                });

        context.start();

        sentToPubSub.sendBody(createExportJob(JobStatus.PROCESSING));

        mockPubSubTopic.expectedBodiesReceived("test");

        Assertions.assertEquals(1, mockPubSubTopic.getExchanges().size());

        mockPubSubTopic.assertIsSatisfied();

        final Exchange exchange = mockPubSubTopic.assertExchangeReceived(0);
        final ExportJob exportJob = exchange.getIn().getBody(ExportJob.class);

        //Assertions.assertEquals(JobStatus.FINISHED, exportJob.getStatus());

        Assertions.assertEquals(NETEX_EXPORT_STATUS_VALUE, exchange.getIn().getHeader(NETEX_EXPORT_STATUS_HEADER));


    }
}

