package org.entur.kingu;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.model.job.ExportJob;
import org.entur.kingu.model.job.JobStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@CamelSpringBootTest
@UseAdviceWith
@ActiveProfiles({"test","local-blobstore", "default", "google-pubsub-autocreate"})
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class KingRouteBuilderIntegrationTestBase {

    private static PubSubEmulatorContainer pubSubEmulatorContainer;

    @Autowired
    protected CamelContext context;

    @Autowired
    protected PubSubTemplate pubSubTemplate;


    protected ExportJob createExportJob(JobStatus jobStatus) throws IOException {
        final Instant start = ZonedDateTime.of(2023,04,22,00,00,00,00, ZoneId.systemDefault()).toInstant();

        final String zipFilePath = "/tmp/tiamat-export.zip";
        final String xmlFilePath = "src/test/resources/org/entur/kingu/route/export/tiamat-export.xml";

        ExportJob exportJob = new ExportJob(jobStatus);
        exportJob.setId(1L);
        exportJob.setJobUrl("export/1");
        exportJob.setFileName("tiamat-export");
        exportJob.setSubFolder("tiamat-export");
        exportJob.setStarted(start);
        exportJob.setLocalExportZipFile(zipFilePath);
        exportJob.setLocalExportXmlFile(xmlFilePath);
        if (jobStatus.equals(JobStatus.FINISHED)) {
            exportJob.setFinished(start.plus(10, ChronoUnit.MINUTES));
        }
        ExportParams exportPrams = new ExportParams();
        exportPrams.setName("tiamat-export");
        exportJob.setExportParams(exportPrams);

        return exportJob;
    }
    @BeforeAll
    public static void init() {
        pubSubEmulatorContainer = new PubSubEmulatorContainer(
                DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:emulators"
                )
        );
        pubSubEmulatorContainer.start();
    }
    @AfterAll
    public static void tearDown() {
        pubSubEmulatorContainer.stop();
    }
    @DynamicPropertySource
    static void pubSubProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gcp.pubsub.emulator-host", pubSubEmulatorContainer::getEmulatorEndpoint);
        registry.add("camel.component.google-pubsub.endpoint", pubSubEmulatorContainer::getEmulatorEndpoint);
    }
}
