package org.entur.kingu.exporter;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.entur.kingu.TestApp;
import org.entur.kingu.config.ExportMode;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.model.EmbeddableMultilingualString;
import org.entur.kingu.model.GroupOfStopPlaces;
import org.entur.kingu.model.Quay;
import org.entur.kingu.model.SiteRefStructure;
import org.entur.kingu.model.StopPlace;
import org.entur.kingu.model.StopPlaceReference;
import org.entur.kingu.model.StopTypeEnumeration;
import org.entur.kingu.model.TariffZone;
import org.entur.kingu.model.TariffZoneRef;
import org.entur.kingu.model.TopographicPlace;
import org.entur.kingu.model.TopographicPlaceRefStructure;
import org.entur.kingu.model.TopographicPlaceTypeEnumeration;
import org.entur.kingu.model.ValidBetween;
import org.entur.kingu.netex.validation.NetexXmlReferenceValidator;
import org.entur.kingu.repository.GroupOfStopPlacesRepository;
import org.entur.kingu.repository.StopPlaceRepository;
import org.entur.kingu.repository.TariffZoneRepository;
import org.entur.kingu.repository.TopographicPlaceRepository;
import org.entur.kingu.repository.search.StopPlaceSearch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TestApp.class)
@CamelSpringBootTest
@UseAdviceWith
@ActiveProfiles({"test", "local-blobstore", "default", "google-pubsub-autocreate"})
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StreamingPublicationDeliveryIntegrationTest {

    private static PubSubEmulatorContainer pubSubEmulatorContainer;

    @Autowired
    private StreamingPublicationDelivery streamingPublicationDelivery;

    @Autowired
    private NetexXmlReferenceValidator netexXmlReferenceValidator;

    @Autowired
    private StopPlaceRepository stopPlaceRepository;

    @Autowired
    private TariffZoneRepository tariffZoneRepository;

    @Autowired
    private TopographicPlaceRepository topographicPlaceRepository;

    @Autowired
    private GroupOfStopPlacesRepository groupOfStopPlacesRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private TopographicPlace savedCounty;
    private TopographicPlace savedMunicipality;
    private TariffZone savedTariffZone;
    private StopPlace savedStopPlace;

    @BeforeAll
    public static void init() {
        pubSubEmulatorContainer = new PubSubEmulatorContainer(
                DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:emulators")
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

    @BeforeEach
    @Transactional
    void setupTestData() {
        // Create county topographic place
        TopographicPlace county = new TopographicPlace();
        county.setNetexId("NSR:TopographicPlace:1");
        county.setVersion(1L);
        county.setName(new EmbeddableMultilingualString("Østfold", "nor"));
        county.setTopographicPlaceType(TopographicPlaceTypeEnumeration.COUNTY);
        savedCounty = topographicPlaceRepository.save(county);

        // Create municipality topographic place with parent
        TopographicPlace municipality = new TopographicPlace();
        municipality.setNetexId("NSR:TopographicPlace:2");
        municipality.setVersion(1L);
        municipality.setName(new EmbeddableMultilingualString("Sarpsborg", "nor"));
        municipality.setTopographicPlaceType(TopographicPlaceTypeEnumeration.MUNICIPALITY);
        municipality.setParentTopographicPlaceRef(new TopographicPlaceRefStructure(savedCounty));
        savedMunicipality = topographicPlaceRepository.save(municipality);

        // Create tariff zone
        TariffZone tariffZone = new TariffZone();
        tariffZone.setNetexId("RUT:TariffZone:100");
        tariffZone.setVersion(1L);
        tariffZone.setName(new EmbeddableMultilingualString("Zone 1", "nor"));
        savedTariffZone = tariffZoneRepository.save(tariffZone);

        // Create quay
        Quay quay = new Quay();
        quay.setNetexId("NSR:Quay:1001");
        quay.setVersion(1L);
        quay.setCentroid(createPoint(10.75, 59.91));

        // Create stop place with quay
        StopPlace stopPlace = new StopPlace();
        stopPlace.setNetexId("NSR:StopPlace:1001");
        stopPlace.setVersion(1L);
        stopPlace.setName(new EmbeddableMultilingualString("Test Stop Place", "nor"));
        stopPlace.setStopPlaceType(StopTypeEnumeration.ONSTREET_BUS);
        stopPlace.setCentroid(createPoint(10.75, 59.91));
        stopPlace.setTopographicPlace(savedMunicipality);
        stopPlace.setValidBetween(new ValidBetween(Instant.now(), null));

        Set<Quay> quays = new HashSet<>();
        quays.add(quay);
        stopPlace.setQuays(quays);

        Set<TariffZoneRef> tariffZoneRefs = new HashSet<>();
        tariffZoneRefs.add(new TariffZoneRef(savedTariffZone));
        stopPlace.setTariffZones(tariffZoneRefs);

        savedStopPlace = stopPlaceRepository.save(stopPlace);

        // Create group of stop places
        GroupOfStopPlaces groupOfStopPlaces = new GroupOfStopPlaces();
        groupOfStopPlaces.setNetexId("NSR:GroupOfStopPlaces:1");
        groupOfStopPlaces.setVersion(1L);
        groupOfStopPlaces.setName(new EmbeddableMultilingualString("Test Group", "nor"));
        groupOfStopPlaces.setCentroid(createPoint(10.75, 59.91));
        groupOfStopPlaces.getMembers().add(new StopPlaceReference(savedStopPlace.getNetexId()));
        groupOfStopPlacesRepository.save(groupOfStopPlaces);
    }

    private ExportParams createExportParams(String name,
                                            ExportMode tariffZoneMode,
                                            ExportMode fareZoneMode,
                                            ExportMode groupOfStopPlacesMode,
                                            ExportMode groupOfTariffZonesMode,
                                            ExportMode topographicPlaceMode) {
        return new ExportParams(
                name,
                tariffZoneMode,
                fareZoneMode,
                groupOfStopPlacesMode,
                groupOfTariffZonesMode,
                topographicPlaceMode,
                null, // municipalityReferences
                null, // countyReferences
                null, // countryReferences
                new StopPlaceSearch()  // default stop place search
        );
    }

    @Test
    void testFullExportWithValidation() throws Exception {
        // Setup export params
        ExportParams exportParams = createExportParams(
                "Integration Test Export",
                ExportMode.ALL,      // tariffZone
                ExportMode.NONE,     // fareZone
                ExportMode.ALL,      // groupOfStopPlaces
                ExportMode.NONE,     // groupOfTariffZones
                ExportMode.ALL       // topographicPlace
        );

        // Execute export
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);

        // Verify XML is not empty
        assertFalse(xmlContent.isEmpty(), "Export should produce non-empty XML");
        assertTrue(xmlContent.contains("PublicationDelivery"), "Should contain PublicationDelivery root element");

        // Write to temp file for validation
        File tempFile = createSecureTempFile("netex-export-test", ".xml");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(xmlContent.getBytes(StandardCharsets.UTF_8));
        }

        // Validate NeTEx references
        netexXmlReferenceValidator.validateNetexReferences(tempFile);

        // Parse and assert content
        assertXmlContent(xmlContent);

        // Cleanup
        tempFile.delete();
    }

    @Test
    void testExportContainsStopPlace() throws Exception {
        ExportParams exportParams = createExportParams(
                "Stop Place Export Test",
                ExportMode.RELEVANT,  // tariffZone
                ExportMode.NONE,      // fareZone
                ExportMode.RELEVANT,  // groupOfStopPlaces
                ExportMode.NONE,      // groupOfTariffZones
                ExportMode.RELEVANT   // topographicPlace
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);

        // Assert stop place is included
        assertTrue(xmlContent.contains("NSR:StopPlace:1001"), "Should contain the test stop place");
        assertTrue(xmlContent.contains("Test Stop Place"), "Should contain stop place name");
        assertTrue(xmlContent.contains("NSR:Quay:1001"), "Should contain the quay");

        // otherTransportModes is @Transient on kingu's StopPlace/Quay; the getter null-initialises to
        // an empty list, so without excluding it from the classMaps Orika/JAXB would marshal a spurious
        // empty <OtherTransportModes></OtherTransportModes> element on every stop place and quay.
        assertFalse(xmlContent.contains("OtherTransportModes"), "Should not contain an empty OtherTransportModes element");
    }

    @Test
    void testExportContainsTariffZone() throws Exception {
        ExportParams exportParams = createExportParams(
                "Tariff Zone Export Test",
                ExportMode.ALL,   // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.NONE,  // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.NONE   // topographicPlace
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);

        // Assert tariff zone is included
        assertTrue(xmlContent.contains("RUT:TariffZone:100"), "Should contain the test tariff zone");
        assertTrue(xmlContent.contains("Zone 1"), "Should contain tariff zone name");
    }

    @Test
    void testExportContainsTopographicPlaces() throws Exception {
        ExportParams exportParams = createExportParams(
                "Topographic Place Export Test",
                ExportMode.NONE,  // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.NONE,  // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.ALL    // topographicPlace
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);

        // Assert topographic places are included
        assertTrue(xmlContent.contains("NSR:TopographicPlace:1"), "Should contain county topographic place");
        assertTrue(xmlContent.contains("Østfold"), "Should contain county name");
        assertTrue(xmlContent.contains("NSR:TopographicPlace:2"), "Should contain municipality topographic place");
        assertTrue(xmlContent.contains("Sarpsborg"), "Should contain municipality name");
    }

    @Test
    void testExportContainsGroupOfStopPlaces() throws Exception {
        ExportParams exportParams = createExportParams(
                "Group of Stop Places Export Test",
                ExportMode.NONE,  // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.ALL,   // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.NONE   // topographicPlace
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);

        // Assert group of stop places is included
        assertTrue(xmlContent.contains("NSR:GroupOfStopPlaces:1"), "Should contain group of stop places");
        assertTrue(xmlContent.contains("Test Group"), "Should contain group name");
    }

    @Test
    void testExportContainsScheduledStopPointsAndAssignments() throws Exception {
        ExportParams exportParams = createExportParams(
                "Service Frame Export Test",
                ExportMode.NONE,  // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.NONE,  // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.NONE   // topographicPlace
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);

        // Assert scheduled stop points are included
        assertTrue(xmlContent.contains("ScheduledStopPoint"), "Should contain ScheduledStopPoint elements");
        assertTrue(xmlContent.contains("PassengerStopAssignment"), "Should contain PassengerStopAssignment elements");
        assertTrue(xmlContent.contains("ServiceFrame"), "Should contain ServiceFrame");
    }

    /**
     * Reproduces the export failure where a multimodal parent stop references a topographic place
     * that none of its children reference.
     *
     * The parent stop is appended to the export by {@link org.entur.kingu.exporter.async.ParentStopFetchingIterator}
     * but is not part of the stop place search result (here the export is requested by the child id). Before the fix,
     * topographic places were gathered only from the search result, so the parent's TopographicPlaceRef had no
     * matching TopographicPlace in the document and reference validation failed on a dangling reference.
     */
    @Test
    void testExportMultimodalParentReferencingTopographicPlaceNotReferencedByChildren() throws Exception {
        TopographicPlace parentMunicipality = new TopographicPlace();
        parentMunicipality.setNetexId("NSR:TopographicPlace:10");
        parentMunicipality.setVersion(1L);
        parentMunicipality.setName(new EmbeddableMultilingualString("Parent municipality", "nor"));
        parentMunicipality.setTopographicPlaceType(TopographicPlaceTypeEnumeration.MUNICIPALITY);
        parentMunicipality.setParentTopographicPlaceRef(new TopographicPlaceRefStructure(savedCounty));
        parentMunicipality = topographicPlaceRepository.save(parentMunicipality);

        TopographicPlace childMunicipality = new TopographicPlace();
        childMunicipality.setNetexId("NSR:TopographicPlace:11");
        childMunicipality.setVersion(1L);
        childMunicipality.setName(new EmbeddableMultilingualString("Child municipality", "nor"));
        childMunicipality.setTopographicPlaceType(TopographicPlaceTypeEnumeration.MUNICIPALITY);
        childMunicipality.setParentTopographicPlaceRef(new TopographicPlaceRefStructure(savedCounty));
        childMunicipality = topographicPlaceRepository.save(childMunicipality);

        // Multimodal parent stop referencing the municipality that none of its children reference.
        StopPlace parent = new StopPlace();
        parent.setNetexId("NSR:StopPlace:2001");
        parent.setVersion(1L);
        parent.setName(new EmbeddableMultilingualString("Multimodal parent", "nor"));
        parent.setParentStopPlace(true);
        parent.setTopographicPlace(parentMunicipality);
        parent.setValidBetween(new ValidBetween(Instant.now(), null));
        stopPlaceRepository.save(parent);

        // Child stop referencing the other municipality, linked to the parent via parentSiteRef.
        StopPlace child = new StopPlace();
        child.setNetexId("NSR:StopPlace:2002");
        child.setVersion(1L);
        child.setName(new EmbeddableMultilingualString("Child stop", "nor"));
        child.setTopographicPlace(childMunicipality);
        child.setParentSiteRef(new SiteRefStructure(parent.getNetexId(), String.valueOf(parent.getVersion())));
        child.setValidBetween(new ValidBetween(Instant.now(), null));
        stopPlaceRepository.save(child);

        StopPlaceSearch stopPlaceSearch = StopPlaceSearch.newStopPlaceSearchBuilder()
                .setNetexIdList(List.of(child.getNetexId()))
                .setAllVersions(true)
                .build();

        ExportParams exportParams = new ExportParams(
                "Multimodal parent topographic place export",
                ExportMode.NONE,      // tariffZone
                ExportMode.NONE,      // fareZone
                ExportMode.NONE,      // groupOfStopPlaces
                ExportMode.NONE,      // groupOfTariffZones
                ExportMode.RELEVANT,  // topographicPlace
                null,
                null,
                null,
                stopPlaceSearch
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);

        // Reference validation fails on a dangling TopographicPlaceRef before the fix.
        File tempFile = createSecureTempFile("netex-export-parent-topo-test", ".xml");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(xmlContent.getBytes(StandardCharsets.UTF_8));
        }
        netexXmlReferenceValidator.validateNetexReferences(tempFile);
        tempFile.delete();

        // Both the searched child and its appended parent must be exported.
        assertTrue(xmlContent.contains("NSR:StopPlace:2001"), "Should contain the appended parent stop place");
        assertTrue(xmlContent.contains("NSR:StopPlace:2002"), "Should contain the searched child stop place");

        // Both municipalities must be present: the one referenced by the child and the one referenced only by the parent.
        assertTrue(xmlContent.contains("NSR:TopographicPlace:10"), "Should contain the topographic place referenced only by the parent");
        assertTrue(xmlContent.contains("NSR:TopographicPlace:11"), "Should contain the topographic place referenced by the child");
    }

    @Test
    void testExportXmlStructure() throws Exception {
        ExportParams exportParams = createExportParams(
                "XML Structure Test",
                ExportMode.ALL,   // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.ALL,   // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.ALL    // topographicPlace
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);

        // Parse XML and verify structure
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

        // Verify root element
        assertEquals("PublicationDelivery", document.getDocumentElement().getLocalName());

        // Verify publication timestamp exists
        NodeList timestampNodes = document.getElementsByTagNameNS("http://www.netex.org.uk/netex", "PublicationTimestamp");
        assertTrue(timestampNodes.getLength() > 0, "Should have PublicationTimestamp");

        // Verify participant ref exists
        NodeList participantRefNodes = document.getElementsByTagNameNS("http://www.netex.org.uk/netex", "ParticipantRef");
        assertTrue(participantRefNodes.getLength() > 0, "Should have ParticipantRef");

        // Verify data objects exist
        NodeList dataObjectsNodes = document.getElementsByTagNameNS("http://www.netex.org.uk/netex", "dataObjects");
        assertTrue(dataObjectsNodes.getLength() > 0, "Should have dataObjects");

        // Verify SiteFrame exists
        NodeList siteFrameNodes = document.getElementsByTagNameNS("http://www.netex.org.uk/netex", "SiteFrame");
        assertTrue(siteFrameNodes.getLength() > 0, "Should have SiteFrame");
    }

    @Test
    void testExportWithMultiSurfaceFlagExportsMultiSurfaceForTopographicPlace() throws Exception {
        // Set multi-surface geometry on the county topographic place
        MultiPolygon multiPolygon = createMultiPolygon();
        savedCounty.setMultiSurface(multiPolygon);
        topographicPlaceRepository.save(savedCounty);

        ExportParams exportParams = createExportParams(
                "MultiSurface TopographicPlace Export",
                ExportMode.NONE,  // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.NONE,  // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.ALL    // topographicPlace
        );
        exportParams.setExportMultiSurface(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);
        Document document = parseXml(xmlContent);

        assertTrue(xmlContent.contains("NSR:TopographicPlace:1"), "Should contain topographic place");
        assertTrue(getGmlElementCount(document, "MultiSurface") > 0, "Should contain MultiSurface element when exportMultiSurface is true");
        // Note: Polygon elements may exist nested inside MultiSurface/surfaceMember - that's expected GML structure
        // The key assertion is that MultiSurface is present (not top-level Polygon as the zone geometry)
    }

    @Test
    void testExportWithoutMultiSurfaceFlagExportsPolygonForTopographicPlace() throws Exception {
        // Set both polygon and multi-surface on the county
        Polygon polygon = createSimplePolygon();
        savedCounty.setPolygon(polygon);
        MultiPolygon multiPolygon = createMultiPolygon();
        savedCounty.setMultiSurface(multiPolygon);
        topographicPlaceRepository.save(savedCounty);

        ExportParams exportParams = createExportParams(
                "Polygon TopographicPlace Export",
                ExportMode.NONE,  // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.NONE,  // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.ALL    // topographicPlace
        );
        // exportMultiSurface defaults to false

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);
        Document document = parseXml(xmlContent);

        assertTrue(xmlContent.contains("NSR:TopographicPlace:1"), "Should contain topographic place");
        assertTrue(getGmlElementCount(document, "Polygon") > 0, "Should contain Polygon element when exportMultiSurface is false");
        assertEquals(0, getGmlElementCount(document, "MultiSurface"), "Should not contain MultiSurface element when exportMultiSurface is false");
    }

    @Test
    void testExportWithMultiSurfaceFlagForTariffZone() throws Exception {
        // Set multi-surface on the tariff zone
        MultiPolygon multiPolygon = createMultiPolygon();
        savedTariffZone.setMultiSurface(multiPolygon);
        tariffZoneRepository.save(savedTariffZone);

        ExportParams exportParams = createExportParams(
                "MultiSurface TariffZone Export",
                ExportMode.ALL,   // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.NONE,  // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.NONE   // topographicPlace
        );
        exportParams.setExportMultiSurface(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);
        Document document = parseXml(xmlContent);

        assertTrue(xmlContent.contains("RUT:TariffZone:100"), "Should contain tariff zone");
        assertTrue(getGmlElementCount(document, "MultiSurface") > 0, "Should contain MultiSurface element for tariff zone");
    }

    @Test
    void testExportWithoutMultiSurfaceFlagExcludesMultiSurfaceForTariffZone() throws Exception {
        // Set both polygon and multi-surface on the tariff zone
        Polygon polygon = createSimplePolygon();
        savedTariffZone.setPolygon(polygon);
        MultiPolygon multiPolygon = createMultiPolygon();
        savedTariffZone.setMultiSurface(multiPolygon);
        tariffZoneRepository.save(savedTariffZone);

        ExportParams exportParams = createExportParams(
                "Polygon TariffZone Export",
                ExportMode.ALL,   // tariffZone
                ExportMode.NONE,  // fareZone
                ExportMode.NONE,  // groupOfStopPlaces
                ExportMode.NONE,  // groupOfTariffZones
                ExportMode.NONE   // topographicPlace
        );
        // exportMultiSurface defaults to false

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingPublicationDelivery.stream(exportParams, outputStream, true);

        String xmlContent = outputStream.toString(StandardCharsets.UTF_8);
        Document document = parseXml(xmlContent);

        assertTrue(xmlContent.contains("RUT:TariffZone:100"), "Should contain tariff zone");
        assertTrue(getGmlElementCount(document, "Polygon") > 0, "Should contain Polygon when exportMultiSurface is false");
        assertEquals(0, getGmlElementCount(document, "MultiSurface"), "Should not contain MultiSurface when exportMultiSurface is false");
    }

    private MultiPolygon createMultiPolygon() {
        Polygon polygon1 = createJtsPolygon(
                new Coordinate(10.0, 59.0),
                new Coordinate(10.0, 59.1),
                new Coordinate(10.1, 59.1),
                new Coordinate(10.1, 59.0),
                new Coordinate(10.0, 59.0)
        );
        Polygon polygon2 = createJtsPolygon(
                new Coordinate(11.0, 60.0),
                new Coordinate(11.0, 60.1),
                new Coordinate(11.1, 60.1),
                new Coordinate(11.1, 60.0),
                new Coordinate(11.0, 60.0)
        );
        return geometryFactory.createMultiPolygon(new Polygon[]{polygon1, polygon2});
    }

    private Polygon createSimplePolygon() {
        return createJtsPolygon(
                new Coordinate(10.0, 59.0),
                new Coordinate(10.0, 59.1),
                new Coordinate(10.1, 59.1),
                new Coordinate(10.1, 59.0),
                new Coordinate(10.0, 59.0)
        );
    }

    private Polygon createJtsPolygon(Coordinate... coordinates) {
        LinearRing ring = geometryFactory.createLinearRing(coordinates);
        return geometryFactory.createPolygon(ring);
    }

    private void assertXmlContent(String xmlContent) throws Exception {
        // Parse the XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

        // Assert root element
        assertNotNull(document.getDocumentElement());
        assertEquals("PublicationDelivery", document.getDocumentElement().getLocalName());

        // Assert stop places are present
        NodeList stopPlaceNodes = document.getElementsByTagNameNS("http://www.netex.org.uk/netex", "StopPlace");
        assertTrue(stopPlaceNodes.getLength() > 0, "Should contain at least one StopPlace");

        // Assert topographic places are present (when mode is ALL)
        NodeList topographicPlaceNodes = document.getElementsByTagNameNS("http://www.netex.org.uk/netex", "TopographicPlace");
        assertTrue(topographicPlaceNodes.getLength() > 0, "Should contain at least one TopographicPlace");

        // Assert version attribute is present on root
        String version = document.getDocumentElement().getAttribute("version");
        assertNotNull(version);
        assertFalse(version.isEmpty(), "Version attribute should not be empty");
    }

    /**
     * Creates a temp file readable/writable only by the owner, to avoid leaking exported NeTEx
     * content to other local users via the shared temp directory's default permissions.
     */
    private File createSecureTempFile(String prefix, String suffix) throws Exception {
        return Files.createTempFile(prefix, suffix,
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))).toFile();
    }

    private Document parseXml(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }

    private int getGmlElementCount(Document document, String localName) {
        return document.getElementsByTagNameNS("http://www.opengis.net/gml/3.2", localName).getLength();
    }

    private Point createPoint(double longitude, double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}