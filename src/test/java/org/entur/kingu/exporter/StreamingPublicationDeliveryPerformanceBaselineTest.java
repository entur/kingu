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

package org.entur.kingu.exporter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.entur.kingu.TestApp;
import org.entur.kingu.config.ExportMode;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.model.EmbeddableMultilingualString;
import org.entur.kingu.model.SiteRefStructure;
import org.entur.kingu.model.StopPlace;
import org.entur.kingu.repository.StopPlaceRepository;
import org.entur.kingu.repository.search.StopPlaceSearch;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Baseline / regression-guard for the number of SQL statements a stop place export issues.
 *
 * There is no prior perf-testing infrastructure in this repo (no JMH, no Hibernate-statistics
 * assertions, no bulk-data fixtures - see the functional {@link StreamingPublicationDeliveryIntegrationTest}
 * which only ever creates a handful of hardcoded entities). This test fills that gap for the one
 * behaviour that matters most for export performance: whether the number of SQL statements grows
 * with the number of multimodal parent stop places, or stays roughly flat.
 *
 * Before the {@code refactoring-export-performance} fix, {@code ParentStopFetchingIterator} queried
 * the database once per unique parent stop place (via findFirstByNetexIdAndVersion), and it did so
 * TWICE per export - once while building the StopPlaces frame, once while building ScheduledStopPoints.
 * That made statement count grow roughly 2x per additional multimodal parent. The fix bulk-loads all
 * parents once (StreamingPublicationDelivery#loadParentsByRef) and reuses the same in-memory stop
 * place list for both frames, so statement count should stay roughly flat as parent count grows.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TestApp.class)
@CamelSpringBootTest
@UseAdviceWith
@ActiveProfiles({"test", "local-blobstore", "default", "google-pubsub-autocreate"})
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StreamingPublicationDeliveryPerformanceBaselineTest {

    private static final Logger logger = LoggerFactory.getLogger(StreamingPublicationDeliveryPerformanceBaselineTest.class);

    private static final int SMALL_PARENT_COUNT = 5;
    private static final int LARGE_PARENT_COUNT = 55;
    private static final int CHILDREN_PER_PARENT = 2;

    private static PubSubEmulatorContainer pubSubEmulatorContainer;

    @Autowired
    private StreamingPublicationDelivery streamingPublicationDelivery;

    @Autowired
    private StopPlaceRepository stopPlaceRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @org.junit.jupiter.api.BeforeAll
    public static void init() {
        pubSubEmulatorContainer = new PubSubEmulatorContainer(
                DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:emulators")
        );
        pubSubEmulatorContainer.start();
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDown() {
        pubSubEmulatorContainer.stop();
    }

    @DynamicPropertySource
    static void pubSubProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gcp.pubsub.emulator-host", pubSubEmulatorContainer::getEmulatorEndpoint);
        registry.add("camel.component.google-pubsub.endpoint", pubSubEmulatorContainer::getEmulatorEndpoint);
    }

    @Test
    @Transactional
    void exportStatementCountShouldNotScaleLinearlyWithMultimodalParentCount() throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        List<String> smallBatchChildIds = createMultimodalHierarchy(SMALL_PARENT_COUNT, CHILDREN_PER_PARENT, "SMALL");
        entityManager.flush();
        statistics.clear();
        runExport(smallBatchChildIds);
        long smallBatchStatementCount = statistics.getPrepareStatementCount();
        logger.info("Prepared statement count for {} multimodal parents ({} stop places): {}",
                SMALL_PARENT_COUNT, smallBatchChildIds.size() + SMALL_PARENT_COUNT, smallBatchStatementCount);

        List<String> largeBatchChildIds = createMultimodalHierarchy(LARGE_PARENT_COUNT, CHILDREN_PER_PARENT, "LARGE");
        entityManager.flush();
        statistics.clear();
        runExport(largeBatchChildIds);
        long largeBatchStatementCount = statistics.getPrepareStatementCount();
        logger.info("Prepared statement count for {} multimodal parents ({} stop places): {}",
                LARGE_PARENT_COUNT, largeBatchChildIds.size() + LARGE_PARENT_COUNT, largeBatchStatementCount);

        long parentCountDelta = LARGE_PARENT_COUNT - SMALL_PARENT_COUNT;
        long statementCountDelta = largeBatchStatementCount - smallBatchStatementCount;

        logger.info("Statement count delta: {} for parent count delta: {} (ratio: {})",
                statementCountDelta, parentCountDelta, statementCountDelta / (double) parentCountDelta);

        // A per-parent query (N+1) regression would grow statement count roughly proportionally to
        // parent count (the old code did so at a rate of ~2 statements per additional parent). The
        // fixed code only adds a small, roughly constant number of extra statements (mostly batched
        // lazy-collection fetches, governed by hibernate.default_batch_fetch_size) as data grows.
        assertTrue(statementCountDelta < parentCountDelta,
                String.format(
                        "Expected statement count growth (%d) to stay well below the growth in multimodal parent count (%d). " +
                        "Growth at or above that rate suggests a per-parent query (N+1) regression in ParentStopFetchingIterator " +
                        "or the stop place export pipeline in StreamingPublicationDelivery.",
                        statementCountDelta, parentCountDelta));
    }

    private void runExport(List<String> childNetexIds) throws Exception {
        StopPlaceSearch stopPlaceSearch = StopPlaceSearch.newStopPlaceSearchBuilder()
                .setNetexIdList(childNetexIds)
                .setAllVersions(true)
                .build();

        ExportParams exportParams = new ExportParams(
                "Performance baseline export",
                ExportMode.NONE,  // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.NONE,  // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.NONE,  // topographicPlace
                null,
                null,
                null,
                stopPlaceSearch
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);
    }

    /**
     * Creates {@code parentCount} multimodal parent stop places, each with {@code childrenPerParent}
     * children linked via parentSiteRef, and returns the children's netex IDs (parents are deliberately
     * excluded from the search result, matching real multimodal exports, and only get pulled in via
     * ParentStopFetchingIterator).
     */
    private List<String> createMultimodalHierarchy(int parentCount, int childrenPerParent, String batchTag) {
        List<String> childNetexIds = new ArrayList<>();
        for (int p = 0; p < parentCount; p++) {
            String parentNetexId = "NSR:StopPlace:PERF-" + batchTag + "-P" + p;

            StopPlace parent = new StopPlace();
            parent.setNetexId(parentNetexId);
            parent.setVersion(1L);
            parent.setName(new EmbeddableMultilingualString("Parent " + batchTag + p, "nor"));
            parent.setParentStopPlace(true);
            parent.setValidBetween(new org.entur.kingu.model.ValidBetween(Instant.now(), null));
            stopPlaceRepository.save(parent);

            for (int c = 0; c < childrenPerParent; c++) {
                String childNetexId = "NSR:StopPlace:PERF-" + batchTag + "-P" + p + "-C" + c;

                StopPlace child = new StopPlace();
                child.setNetexId(childNetexId);
                child.setVersion(1L);
                child.setName(new EmbeddableMultilingualString("Child " + batchTag + p + "-" + c, "nor"));
                child.setParentSiteRef(new SiteRefStructure(parentNetexId, "1"));
                child.setValidBetween(new org.entur.kingu.model.ValidBetween(Instant.now(), null));
                stopPlaceRepository.save(child);

                childNetexIds.add(childNetexId);
            }
        }
        return childNetexIds;
    }
}
