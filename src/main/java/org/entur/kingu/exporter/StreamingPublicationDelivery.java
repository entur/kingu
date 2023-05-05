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

import org.entur.kingu.config.ExportMode;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.exporter.async.NetexMappingIterator;
import org.entur.kingu.exporter.async.NetexMappingIteratorList;
import org.entur.kingu.exporter.async.NetexReferenceRemovingIterator;
import org.entur.kingu.exporter.async.ParentStopFetchingIterator;
import org.entur.kingu.exporter.async.ParentTreeTopographicPlaceFetchingIterator;
import org.entur.kingu.model.FareFrame;
import org.entur.kingu.model.GroupOfStopPlaces;
import org.entur.kingu.model.GroupOfTariffZones;
import org.entur.kingu.model.Quay;
import org.entur.kingu.model.ServiceFrame;
import org.entur.kingu.model.TopographicPlace;
import org.entur.kingu.netex.id.NetexIdHelper;
import org.entur.kingu.netex.id.ValidPrefixList;
import org.entur.kingu.netex.mapping.NetexMapper;
import org.entur.kingu.repository.FareZoneRepository;
import org.entur.kingu.repository.GroupOfStopPlacesRepository;
import org.entur.kingu.repository.GroupOfTariffZonesRepository;
import org.entur.kingu.repository.ParkingRepository;
import org.entur.kingu.repository.StopPlaceRepository;
import org.entur.kingu.repository.TariffZoneRepository;
import org.entur.kingu.repository.TopographicPlaceRepository;
import org.entur.kingu.service.PrometheusMetricsService;
import org.rutebanken.netex.model.FareZone;
import org.rutebanken.netex.model.FareZonesInFrame_RelStructure;
import org.rutebanken.netex.model.GroupsOfStopPlacesInFrame_RelStructure;
import org.rutebanken.netex.model.GroupsOfTariffZonesInFrame_RelStructure;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.Parking;
import org.rutebanken.netex.model.ParkingsInFrame_RelStructure;
import org.rutebanken.netex.model.PassengerStopAssignment;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.QuayRefStructure;
import org.rutebanken.netex.model.ScheduledStopPoint;
import org.rutebanken.netex.model.ScheduledStopPointRefStructure;
import org.rutebanken.netex.model.ScheduledStopPointsInFrame_RelStructure;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopAssignment_VersionStructure;
import org.rutebanken.netex.model.StopAssignmentsInFrame_RelStructure;
import org.rutebanken.netex.model.StopPlace;
import org.rutebanken.netex.model.StopPlaceRefStructure;
import org.rutebanken.netex.model.StopPlacesInFrame_RelStructure;
import org.rutebanken.netex.model.TariffZone;
import org.rutebanken.netex.model.TariffZonesInFrame_RelStructure;
import org.rutebanken.netex.model.TopographicPlacesInFrame_RelStructure;
import org.rutebanken.netex.model.ValidBetween;
import org.rutebanken.netex.model.Zone_VersionStructure;
import org.rutebanken.netex.validation.NeTExValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static javax.xml.bind.JAXBContext.newInstance;

/**
 * Stream data objects inside already serialized publication delivery.
 * To be able to export many stop places wihtout keeping them all in memory.
 */
@Transactional(readOnly = true)
@Component
public class StreamingPublicationDelivery {

    private static final Logger logger = LoggerFactory.getLogger(StreamingPublicationDelivery.class);

    @Value("${netex.profile.version:1.12:NO-NeTEx-stops:1.4}")
    private String publicationDeliveryId;

    private static final JAXBContext publicationDeliveryContext = createContext(PublicationDeliveryStructure.class);
    private static final ObjectFactory netexObjectFactory = new ObjectFactory();

    private final StopPlaceRepository stopPlaceRepository;
    private final ParkingRepository parkingRepository;
    private final ValidPrefixList validPrefixList;
    private final PublicationDeliveryHelper publicationDeliveryHelper;
    private final NetexMapper netexMapper;
    private final TariffZoneRepository tariffZoneRepository;
    private final FareZoneRepository fareZoneRepository;
    private final TopographicPlaceRepository topographicPlaceRepository;
    private final GroupOfStopPlacesRepository groupOfStopPlacesRepository;
    private final GroupOfTariffZonesRepository groupOfTariffZonesRepository;
    private final NeTExValidator neTExValidator = NeTExValidator.getNeTExValidator();
    private final NetexIdHelper netexIdHelper;
    /**
     * Validate against netex schema using the {@link NeTExValidator}
     * Enabling this for large xml files can lead to high memory consumption and/or massive performance impact.
     */
    private final boolean validateAgainstSchema;

    private final PrometheusMetricsService prometheusMetricsService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public StreamingPublicationDelivery(StopPlaceRepository stopPlaceRepository,
                                        ParkingRepository parkingRepository,
                                        ValidPrefixList validPrefixList,
                                        PublicationDeliveryHelper publicationDeliveryHelper,
                                        NetexMapper netexMapper,
                                        TariffZoneRepository tariffZoneRepository,
                                        FareZoneRepository fareZoneRepository,
                                        TopographicPlaceRepository topographicPlaceRepository,
                                        GroupOfStopPlacesRepository groupOfStopPlacesRepository,
                                        GroupOfTariffZonesRepository groupOfTariffZonesRepository,
                                        NetexIdHelper netexIdHelper,
                                        @Value("${asyncNetexExport.validateAgainstSchema:false}") boolean validateAgainstSchema,
                                        PrometheusMetricsService prometheusMetricsService) throws IOException, SAXException {
        this.stopPlaceRepository = stopPlaceRepository;
        this.parkingRepository = parkingRepository;
        this.validPrefixList = validPrefixList;
        this.publicationDeliveryHelper = publicationDeliveryHelper;
        this.netexMapper = netexMapper;
        this.tariffZoneRepository = tariffZoneRepository;
        this.fareZoneRepository = fareZoneRepository;
        this.topographicPlaceRepository = topographicPlaceRepository;
        this.groupOfStopPlacesRepository = groupOfStopPlacesRepository;
        this.groupOfTariffZonesRepository = groupOfTariffZonesRepository;
        this.netexIdHelper = netexIdHelper;
        this.validateAgainstSchema = validateAgainstSchema;
        this.prometheusMetricsService = prometheusMetricsService;
    }

    private static JAXBContext createContext(Class clazz) {
        try {
            JAXBContext jaxbContext = newInstance(clazz);
            logger.info("Created context {}", jaxbContext.getClass());
            return jaxbContext;
        } catch (JAXBException e) {
            String message = "Could not create instance of jaxb context for class " + clazz;
            logger.warn(message, e);
            throw new RuntimeException("Could not create instance of jaxb context for class " + clazz, e);
        }
    }

    public void stream(ExportParams exportParams, OutputStream outputStream) throws JAXBException, XMLStreamException, IOException, InterruptedException, SAXException {
        stream(exportParams, outputStream, false);
    }

    public void stream(ExportParams exportParams, OutputStream outputStream, boolean ignorePaging) throws JAXBException, XMLStreamException, IOException, InterruptedException, SAXException {

        org.entur.kingu.model.SiteFrame siteFrame = publicationDeliveryHelper.createTiamatSiteFrame("Site frame " + exportParams);
        final ServiceFrame serviceFrame = publicationDeliveryHelper.createTiamatServiceFrame("Service frame " + exportParams);
        final FareFrame fareFrame = publicationDeliveryHelper.createTiamatFareFrame("Fare frame " + exportParams);

        AtomicInteger mappedStopPlaceCount = new AtomicInteger();
        AtomicInteger mappedParkingCount = new AtomicInteger();
        AtomicInteger mappedTariffZonesCount = new AtomicInteger();
        AtomicInteger mappedFareZonesCount = new AtomicInteger();
        AtomicInteger mappedTopographicPlacesCount = new AtomicInteger();
        AtomicInteger mappedGroupOfStopPlacesCount = new AtomicInteger();
        AtomicInteger mappedGroupOfTariffZonesCount = new AtomicInteger();

        logger.info("Streaming export initiated. Export params: {}", exportParams);

        // We need to know these IDs before marshalling begins.
        // To avoid marshalling empty parking element and to be able to gather relevant topographic places
        // The primary ID represents a stop place with a certain version
        var start = System.currentTimeMillis();
        final List<org.entur.kingu.model.StopPlace> allStopPlaces =  stopPlaceRepository.findAllStopPlaces(exportParams, ignorePaging);
        var end = System.currentTimeMillis();
        var duration =  (end-start)/1000L;
        logger.info("Got {} stops from stop place search in {} secs", allStopPlaces.size(),duration);
        final Set<Long> stopPlacePrimaryIds = allStopPlaces.stream().map(stopPlace -> stopPlace.getId()).collect(Collectors.toSet());


        logger.info("Got {} stop place IDs from stop place search", stopPlacePrimaryIds.size());


        logger.info("Mapping site frame to netex model");
        org.rutebanken.netex.model.SiteFrame netexSiteFrame = netexMapper.mapToNetexModel(siteFrame);

        logger.info("Mapping service frame to netex model");
        final org.rutebanken.netex.model.ServiceFrame netexServiceFrame = netexMapper.mapToNetexModel(serviceFrame);

        logger.info("Mapping fare frame to netex model");
        final org.rutebanken.netex.model.FareFrame netexFareFrame = netexMapper.mapToNetexModel(fareFrame);


        logger.info("Preparing scrollable iterators");
        prepareStopPlaces(exportParams, allStopPlaces, mappedStopPlaceCount, netexSiteFrame);
        prepareTopographicPlaces(exportParams, stopPlacePrimaryIds, mappedTopographicPlacesCount, netexSiteFrame);
        prepareTariffZones(exportParams, stopPlacePrimaryIds, mappedTariffZonesCount, netexSiteFrame);
        prepareParkings(exportParams, stopPlacePrimaryIds, mappedParkingCount, netexSiteFrame);
        prepareGroupOfStopPlaces(exportParams, stopPlacePrimaryIds, mappedGroupOfStopPlacesCount, netexSiteFrame);


        PublicationDeliveryStructure publicationDeliveryStructure;

        if (exportParams.getServiceFrameExportMode() == ExportMode.ALL) {
            prepareFareZones(exportParams,stopPlacePrimaryIds,mappedFareZonesCount,mappedGroupOfTariffZonesCount,netexSiteFrame,netexFareFrame);
            prepareScheduledStopPoints(stopPlacePrimaryIds, netexServiceFrame);
            publicationDeliveryStructure = createPublicationDelivery(netexSiteFrame, netexServiceFrame,netexFareFrame);
        } else {
            publicationDeliveryStructure = createPublicationDelivery(netexSiteFrame);
        }

        Marshaller marshaller = createMarshaller();

        logger.info("Start marshalling publication delivery");
        marshaller.marshal(netexObjectFactory.createPublicationDelivery(publicationDeliveryStructure), outputStream);
        logger.info("Mapped {} stop places, {} parkings, {} topographic places, {} group of stop places and {} tariff zones to netex",
                mappedStopPlaceCount.get(),
                mappedParkingCount.get(),
                mappedTopographicPlacesCount,
                mappedGroupOfStopPlacesCount,
                mappedTariffZonesCount);

    }

    private void prepareFareZones(ExportParams exportParams,Set<Long> stopPlacePrimaryIds, AtomicInteger mappedFareZonesCount, AtomicInteger mappedGroupOfTariffZonesCount, SiteFrame netexSiteFrame, org.rutebanken.netex.model.FareFrame netexFareFrame) {

        boolean exportGroupOfTariffZones =false;

        Iterator<org.entur.kingu.model.FareZone> fareZoneIterator;
        if (exportParams.getFareZoneExportMode() == null || exportParams.getFareZoneExportMode().equals(ExportMode.ALL)) {
            logger.info("Preparing to scroll fare zones, regardless of version");
            fareZoneIterator = fareZoneRepository.scrollFareZones(exportParams);
            exportGroupOfTariffZones = true;
        } else if (exportParams.getFareZoneExportMode().equals(ExportMode.RELEVANT)) {
            int fareZoneCount = fareZoneRepository.countResult(stopPlacePrimaryIds);
            logger.info("Preparing to scroll {} relevant fare zones from stop place ids", fareZoneCount);
            fareZoneIterator = fareZoneRepository.scrollFareZones(stopPlacePrimaryIds);

            exportGroupOfTariffZones = true;
        } else {
            logger.info("Fare zone export mode is {}. Will not export fare zones", exportParams.getFareZoneExportMode());
            fareZoneIterator = Collections.emptyIterator();
        }
            var fareZonesInFrameRelStructure = new FareZonesInFrame_RelStructure();
            List<FareZone> netexFareZone = new NetexMappingIteratorList<>(() -> new NetexMappingIterator<>(netexMapper, fareZoneIterator,
                    FareZone.class, mappedFareZonesCount,prometheusMetricsService,exportParams.getName()));

            setField(FareZonesInFrame_RelStructure.class,"fareZone", fareZonesInFrameRelStructure, netexFareZone);
            netexFareFrame.setFareZones(fareZonesInFrameRelStructure);

        if (exportGroupOfTariffZones) {
            prepareGroupOfTariffZones(exportParams,stopPlacePrimaryIds,mappedGroupOfTariffZonesCount,netexSiteFrame);
        }
    }

    private void prepareTariffZones(ExportParams exportParams, Set<Long> stopPlacePrimaryIds, AtomicInteger mappedTariffZonesCount, SiteFrame netexSiteFrame) {


        List<org.entur.kingu.model.TariffZone> tariffZoneIterator;
        if (exportParams.getTariffZoneExportMode() == null || exportParams.getTariffZoneExportMode().equals(ExportMode.ALL)) {

            logger.info("Preparing to scroll all tariff zones, regardless of version");
            tariffZoneIterator = tariffZoneRepository.findTariffZones(exportParams);
        } else if (exportParams.getTariffZoneExportMode().equals(ExportMode.RELEVANT)) {

            logger.info("Preparing to scroll relevant tariff zones from stop place ids");
            var start = System.currentTimeMillis();
            tariffZoneIterator = tariffZoneRepository.getTariffZonesFromStopPlaceIds(stopPlacePrimaryIds);
            var end = System.currentTimeMillis();
            var duration =(end-start)/1000L;
            logger.info("Got {} TariffZone in {} secs",tariffZoneIterator.size(),duration);
        } else {
            logger.info("Tariff zone export mode is {}. Will not export tariff zones", exportParams.getTariffZoneExportMode());
            tariffZoneIterator = Collections.emptyList();
        }

        List<JAXBElement<? extends Zone_VersionStructure>> netexTariffZones = new ArrayList<>();
        for (org.entur.kingu.model.TariffZone tiamatTariffZone: tariffZoneIterator) {
            final TariffZone tariffZone = netexMapper.mapToNetexModel(tiamatTariffZone);
            final JAXBElement<TariffZone> tariffZoneJAXBElement = new ObjectFactory().createTariffZone(tariffZone);
            netexTariffZones.add(tariffZoneJAXBElement);
            mappedTariffZonesCount.incrementAndGet();

        }
        if (!netexTariffZones.isEmpty()) {
            var tariffZonesInFrameRelStructure = new TariffZonesInFrame_RelStructure();
            setField(TariffZonesInFrame_RelStructure.class, "tariffZone", tariffZonesInFrameRelStructure, netexTariffZones);
            netexSiteFrame.setTariffZones(tariffZonesInFrameRelStructure);
        } else {
            logger.info("No tariff zones to export");
            netexSiteFrame.setTariffZones(null);
        }

    }

    private void prepareParkings(ExportParams exportParams, Set<Long> stopPlacePrimaryIds, AtomicInteger mappedParkingCount, SiteFrame netexSiteFrame) {

        // ExportParams could be used for parkingExportMode.

        int parkingsCount = parkingRepository.countResult(stopPlacePrimaryIds);
        if (parkingsCount > 0) {
            // Only set parkings if they will exist during marshalling.
            logger.info("Parking count is {}, will create parking in publication delivery", parkingsCount);
            ParkingsInFrame_RelStructure parkingsInFrame_relStructure = new ParkingsInFrame_RelStructure();
            List<Parking> parkings = new NetexMappingIteratorList<>(() -> new NetexMappingIterator<>(netexMapper, parkingRepository.scrollParkings(stopPlacePrimaryIds),
                    Parking.class, mappedParkingCount,prometheusMetricsService,exportParams.getName()));

            setField(ParkingsInFrame_RelStructure.class, "parking", parkingsInFrame_relStructure, parkings);
            netexSiteFrame.setParkings(parkingsInFrame_relStructure);
        } else {
            logger.info("No parkings to export based on stop places");
        }
    }

    private void prepareStopPlaces(ExportParams exportParams, List<org.entur.kingu.model.StopPlace> allStopPlaces, AtomicInteger mappedStopPlaceCount, SiteFrame netexSiteFrame) {

        if (!allStopPlaces.isEmpty()) {
            logger.info("There are stop places to export");

            StopPlacesInFrame_RelStructure stopPlacesInFrame_relStructure = new StopPlacesInFrame_RelStructure();

            // a map netex_ids and version of all current stop places at start of export

            final Map<String, String> allCurrentNetexIdsAndVersion = stopPlaceRepository.findAllCurrentNetexIdsAndVersion();

            logger.info("current stop places netex ids size: {}",allCurrentNetexIdsAndVersion.size());


            // Use Listening iterator to collect stop place IDs.
            ParentStopFetchingIterator parentStopFetchingIterator = new ParentStopFetchingIterator(allStopPlaces.iterator(), stopPlaceRepository);
            NetexMappingIterator<org.entur.kingu.model.StopPlace, StopPlace> netexMappingIterator = new NetexMappingIterator<>(netexMapper, parentStopFetchingIterator, StopPlace.class, mappedStopPlaceCount, prometheusMetricsService,exportParams.getName());

            List<StopPlace> stopPlaces = new NetexMappingIteratorList<>(() -> new NetexReferenceRemovingIterator(netexMappingIterator, exportParams, allCurrentNetexIdsAndVersion));

            setField(StopPlacesInFrame_RelStructure.class, "stopPlace", stopPlacesInFrame_relStructure, stopPlaces);
            netexSiteFrame.setStopPlaces(stopPlacesInFrame_relStructure);
        } else {
            logger.info("No stop places to export");
        }
    }

    private void prepareScheduledStopPoints(Set<Long> stopPlacePrimaryIds, org.rutebanken.netex.model.ServiceFrame netexServiceFrame) {
        if (!stopPlacePrimaryIds.isEmpty()) {
            logger.info("There are stop places to export");

            final Iterator<org.entur.kingu.model.StopPlace> stopPlaceIterator = stopPlaceRepository.scrollStopPlaces(stopPlacePrimaryIds);

            // Use Listening iterator to collect stop place IDs.
            ParentStopFetchingIterator parentStopFetchingIterator = new ParentStopFetchingIterator(stopPlaceIterator, stopPlaceRepository);

            List<ScheduledStopPoint> netexScheduledStopPoints = new ArrayList<>();

            List<JAXBElement<? extends StopAssignment_VersionStructure>> stopAssignment = new ArrayList<>();


            while (parentStopFetchingIterator.hasNext()) {
                final org.entur.kingu.model.StopPlace stopPlace = parentStopFetchingIterator.next();
                covertStopPlaceToScheduledStopPoint(netexScheduledStopPoints, stopAssignment, stopPlace);

            }


            if (!netexScheduledStopPoints.isEmpty()) {
                final ScheduledStopPointsInFrame_RelStructure scheduledStopPointsInFrame_relStructure = new ScheduledStopPointsInFrame_RelStructure();
                setField(ScheduledStopPointsInFrame_RelStructure.class, "scheduledStopPoint", scheduledStopPointsInFrame_relStructure, netexScheduledStopPoints);
                netexServiceFrame.setScheduledStopPoints(scheduledStopPointsInFrame_relStructure);


                StopAssignmentsInFrame_RelStructure stopAssignmentsInFrame_RelStructure = new StopAssignmentsInFrame_RelStructure();
                setField(StopAssignmentsInFrame_RelStructure.class, "stopAssignment", stopAssignmentsInFrame_RelStructure, stopAssignment);
                netexServiceFrame.setStopAssignments(stopAssignmentsInFrame_RelStructure);
            }

        }
    }

    private void covertStopPlaceToScheduledStopPoint(List<ScheduledStopPoint> scheduledStopPoints, List<JAXBElement<? extends StopAssignment_VersionStructure>> netexPassengerStopAssignment, org.entur.kingu.model.StopPlace stopPlace) {

        // Add stop place

        final String netexId = stopPlace.getNetexId();
        String stopPlaceName = null;
        if (stopPlace.getName() != null) {
            stopPlaceName = stopPlace.getName().getValue();
        }
        final long version = stopPlace.getVersion();
        var stopPlaceNetexId = netexIdHelper.extractIdPostfix(netexId);
        var idPrefix = netexIdHelper.extractIdPrefix(netexId);
        var scheduledStopPointNetexId = idPrefix + ":ScheduledStopPoint:S" + stopPlaceNetexId;

        LocalDateTime validFrom = null;
        LocalDateTime validTo = null;
        if (stopPlace.getValidBetween() != null) {
            if (stopPlace.getValidBetween().getFromDate() != null) {
                validFrom = LocalDateTime.ofInstant(stopPlace.getValidBetween().getFromDate(), ZoneId.systemDefault());
            }
            if (stopPlace.getValidBetween().getToDate() != null) {
                validTo = LocalDateTime.ofInstant(stopPlace.getValidBetween().getToDate(), ZoneId.systemDefault());
            }
        }


        scheduledStopPoints.add(createNetexScheduledStopPoint(scheduledStopPointNetexId, stopPlaceName, version, validFrom, validTo));

        netexPassengerStopAssignment.add(createPassengerStopAssignment(netexId, version, scheduledStopPointNetexId, netexPassengerStopAssignment.size() + 1, validFrom, validTo, false));

        // Add quays
        final Set<Quay> quays = stopPlace.getQuays();
        for (Quay quay : quays) {
            var quayNetexId = netexIdHelper.extractIdPostfix(quay.getNetexId());
            var quayUdPrefix = netexIdHelper.extractIdPrefix(quay.getNetexId());
            var quayScheduledStopPointNetexId = quayUdPrefix + ":ScheduledStopPoint:Q" + quayNetexId;
            scheduledStopPoints.add(createNetexScheduledStopPoint(quayScheduledStopPointNetexId, stopPlaceName, quay.getVersion(), validFrom, validTo));
            netexPassengerStopAssignment.add(createPassengerStopAssignment(quay.getNetexId(), quay.getVersion(), quayScheduledStopPointNetexId, netexPassengerStopAssignment.size() + 1, validFrom, validTo, true));

        }

    }

    private JAXBElement<? extends StopAssignment_VersionStructure> createPassengerStopAssignment(String netexId, long version, String scheduledStopPointNetexId, int passengerStopAssignmentOrder, LocalDateTime validFrom, LocalDateTime validTo, boolean isQuay) {

        var passengerStopAssignmentId = netexIdHelper.extractIdPostfix(scheduledStopPointNetexId);
        var idPrefix= netexIdHelper.extractIdPrefix(scheduledStopPointNetexId);
        final PassengerStopAssignment passengerStopAssignment = new PassengerStopAssignment();
        passengerStopAssignment.withId(idPrefix + ":PassengerStopAssignment:P" + passengerStopAssignmentId);
        passengerStopAssignment.withVersion(String.valueOf(version));
        passengerStopAssignment.withOrder(BigInteger.valueOf(passengerStopAssignmentOrder));

        ValidBetween validBetween = new ValidBetween().withFromDate(validFrom).withToDate(validTo);
        passengerStopAssignment.withValidBetween(validBetween);
        if (isQuay) {
            passengerStopAssignment.withQuayRef(new QuayRefStructure().withRef(netexId).withVersion(String.valueOf(version)));
        } else {
            passengerStopAssignment.withStopPlaceRef(new StopPlaceRefStructure().withRef(netexId).withVersion(String.valueOf(version)));
        }
        final JAXBElement<ScheduledStopPointRefStructure> scheduledStopPointRef = new ObjectFactory().createScheduledStopPointRef(new ScheduledStopPointRefStructure().withRef(scheduledStopPointNetexId).withVersionRef(String.valueOf(version)));
        passengerStopAssignment.withScheduledStopPointRef(scheduledStopPointRef);

        return new ObjectFactory().createPassengerStopAssignment(passengerStopAssignment);

    }

    private ScheduledStopPoint createNetexScheduledStopPoint(String scheduledStopPointNetexId, String stopPlaceName, long version, LocalDateTime validFrom, LocalDateTime validTo) {
        final org.rutebanken.netex.model.ScheduledStopPoint netexScheduledStopPoint = new org.rutebanken.netex.model.ScheduledStopPoint();
        netexScheduledStopPoint.setId(scheduledStopPointNetexId);
        netexScheduledStopPoint.setVersion(String.valueOf(version));
        netexScheduledStopPoint.withName(new MultilingualString().withValue(stopPlaceName));
        ValidBetween validBetween = new ValidBetween().withFromDate(validFrom).withToDate(validTo);

        netexScheduledStopPoint.withValidBetween(validBetween);

        return netexScheduledStopPoint;
    }

    private void prepareTopographicPlaces(ExportParams exportParams, Set<Long> stopPlacePrimaryIds, AtomicInteger mappedTopographicPlacesCount, SiteFrame netexSiteFrame) {

        Iterator<TopographicPlace> relevantTopographicPlacesIterator;

        if (exportParams.getTopographicPlaceExportMode() == null || exportParams.getTopographicPlaceExportMode().equals(ExportMode.ALL)) {
            logger.info("Prepare scrolling for all topographic places");
            relevantTopographicPlacesIterator = topographicPlaceRepository.scrollTopographicPlaces();

        } else if (exportParams.getTopographicPlaceExportMode().equals(ExportMode.RELEVANT)) {
            logger.info("Prepare scrolling relevant topographic places");
            relevantTopographicPlacesIterator = new ParentTreeTopographicPlaceFetchingIterator(topographicPlaceRepository.scrollTopographicPlaces(stopPlacePrimaryIds), topographicPlaceRepository);
        } else {
            logger.info("Topographic export mode is {}. Will not export topographic places", exportParams.getTopographicPlaceExportMode());
            relevantTopographicPlacesIterator = Collections.emptyIterator();
        }

        if (relevantTopographicPlacesIterator.hasNext()) {

            NetexMappingIterator<TopographicPlace, org.rutebanken.netex.model.TopographicPlace> topographicPlaceNetexMappingIterator = new NetexMappingIterator<>(
                    netexMapper, relevantTopographicPlacesIterator, org.rutebanken.netex.model.TopographicPlace.class, mappedTopographicPlacesCount, prometheusMetricsService, exportParams.getName());

            List<org.rutebanken.netex.model.TopographicPlace> topographicPlaces = new NetexMappingIteratorList<>(() -> topographicPlaceNetexMappingIterator);

            TopographicPlacesInFrame_RelStructure topographicPlacesInFrame_relStructure = new TopographicPlacesInFrame_RelStructure();
            setField(TopographicPlacesInFrame_RelStructure.class, "topographicPlace", topographicPlacesInFrame_relStructure, topographicPlaces);
            netexSiteFrame.setTopographicPlaces(topographicPlacesInFrame_relStructure);
        } else {
            netexSiteFrame.setTopographicPlaces(null);
        }
    }

    private void prepareGroupOfStopPlaces(ExportParams exportParams, Set<Long> stopPlacePrimaryIds, AtomicInteger mappedGroupOfStopPlacesCount, SiteFrame netexSiteFrame) {

        Iterator<GroupOfStopPlaces> groupOfStopPlacesIterator;

        if (exportParams.getGroupOfStopPlacesExportMode() == null || exportParams.getGroupOfStopPlacesExportMode().equals(ExportMode.ALL)) {
            logger.info("Prepare scrolling for all group of stop places");
            groupOfStopPlacesIterator = groupOfStopPlacesRepository.scrollGroupOfStopPlaces();

        } else if (exportParams.getGroupOfStopPlacesExportMode().equals(ExportMode.RELEVANT)) {
            logger.info("Prepare scrolling relevant group of stop places");
            groupOfStopPlacesIterator = groupOfStopPlacesRepository.scrollGroupOfStopPlaces(stopPlacePrimaryIds);
        } else {
            logger.info("Group of stop places export mode is {}. Will not export group of stop places", exportParams.getGroupOfStopPlacesExportMode());
            groupOfStopPlacesIterator = Collections.emptyIterator();
        }

        if (groupOfStopPlacesIterator.hasNext()) {

            NetexMappingIterator<GroupOfStopPlaces, org.rutebanken.netex.model.GroupOfStopPlaces> netexMappingIterator = new NetexMappingIterator<>(
                    netexMapper, groupOfStopPlacesIterator, org.rutebanken.netex.model.GroupOfStopPlaces.class, mappedGroupOfStopPlacesCount, prometheusMetricsService, exportParams.getName());

            List<org.rutebanken.netex.model.GroupOfStopPlaces> groupOfStopPlacesList = new NetexMappingIteratorList<>(() -> netexMappingIterator);

            GroupsOfStopPlacesInFrame_RelStructure groupsOfStopPlacesInFrame_relStructure = new GroupsOfStopPlacesInFrame_RelStructure();
            setField(GroupsOfStopPlacesInFrame_RelStructure.class, "groupOfStopPlaces", groupsOfStopPlacesInFrame_relStructure, groupOfStopPlacesList);
            netexSiteFrame.setGroupsOfStopPlaces(groupsOfStopPlacesInFrame_relStructure);
        } else {
            netexSiteFrame.setGroupsOfStopPlaces(null);
        }
    }

    private void prepareGroupOfTariffZones(ExportParams exportParams, Set<Long> stopPlaceIds, AtomicInteger mappedGroupOfTariffZonesCount, SiteFrame netexSiteFrame) {
        Iterator<GroupOfTariffZones> groupOfTariffZonesIterator;
        if (exportParams.getGroupOfTariffZonesExportMode() == null || exportParams.getGroupOfTariffZonesExportMode().equals(ExportMode.ALL)) {
            logger.info("Prepare scrolling for all group of tariff zones");
            groupOfTariffZonesIterator = groupOfTariffZonesRepository.scrollGroupOfTariffZones();
        } else if (exportParams.getGroupOfTariffZonesExportMode().equals(ExportMode.RELEVANT)) {
            logger.info("Prepare scrolling relevant group of tariff zones");
            groupOfTariffZonesIterator = groupOfTariffZonesRepository.scrollGroupOfTariffZones(stopPlaceIds);

        } else {
            logger.info("Group of tariff zones export mode is {}. Will not export group of tariff zones", exportParams.getGroupOfStopPlacesExportMode());
            groupOfTariffZonesIterator = Collections.emptyIterator();
        }

        if (groupOfTariffZonesIterator.hasNext()) {
            NetexMappingIterator<GroupOfTariffZones, org.rutebanken.netex.model.GroupOfTariffZones> netexMappingIterator = new NetexMappingIterator<>(
                    netexMapper, groupOfTariffZonesIterator, org.rutebanken.netex.model.GroupOfTariffZones.class,mappedGroupOfTariffZonesCount, prometheusMetricsService, exportParams.getName());

            List<org.rutebanken.netex.model.GroupOfTariffZones> groupOfTariffZonesList = new NetexMappingIteratorList<>(() -> netexMappingIterator);

            final GroupsOfTariffZonesInFrame_RelStructure groupsOfTariffZonesInFrame_relStructure = new GroupsOfTariffZonesInFrame_RelStructure();
            setField(GroupsOfTariffZonesInFrame_RelStructure.class,"groupOfTariffZones", groupsOfTariffZonesInFrame_relStructure,groupOfTariffZonesList);
            netexSiteFrame.setGroupsOfTariffZones(groupsOfTariffZonesInFrame_relStructure);
        }

    }


    /**
     * Set field value with reflection.
     * Used for setting list values in netex model.
     */
    private void setField(Class clazz, String fieldName, Object instance, Object fieldValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, fieldValue);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Cannot set field " + fieldName + " of " + instance, e);
        }
    }

    private Marshaller createMarshaller() throws JAXBException, IOException, SAXException {
        Marshaller marshaller = publicationDeliveryContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");

        if (validateAgainstSchema) {
            marshaller.setSchema(neTExValidator.getSchema());
        }

        return marshaller;
    }

    @SuppressWarnings("unchecked")
    public PublicationDeliveryStructure createPublicationDelivery(org.rutebanken.netex.model.SiteFrame siteFrame,
                                                                  org.rutebanken.netex.model.ServiceFrame serviceFrame,
                                                                  org.rutebanken.netex.model.FareFrame fareFrame) {
        PublicationDeliveryStructure publicationDeliveryStructure = createPublicationDelivery();

        publicationDeliveryStructure.withDataObjects
                (
                        new PublicationDeliveryStructure.DataObjects()
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createServiceFrame(serviceFrame))
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createSiteFrame(siteFrame))
                                .withCompositeFrameOrCommonFrame(new ObjectFactory().createFareFrame(fareFrame))

                );

        logger.info("Returning publication delivery {} with site frame and  service frame", publicationDeliveryStructure);
        return publicationDeliveryStructure;
    }
    @SuppressWarnings("unchecked")
    private PublicationDeliveryStructure createPublicationDelivery(org.rutebanken.netex.model.SiteFrame siteFrame) {
        PublicationDeliveryStructure publicationDeliveryStructure = createPublicationDelivery();
        publicationDeliveryStructure.withDataObjects(
                new PublicationDeliveryStructure.DataObjects()
                        .withCompositeFrameOrCommonFrame(new ObjectFactory().createSiteFrame(siteFrame)));

        logger.info("Returning publication delivery {} with site frame", publicationDeliveryStructure);
        return publicationDeliveryStructure;
    }
    private PublicationDeliveryStructure createPublicationDelivery() {
        return new PublicationDeliveryStructure()
                .withVersion(publicationDeliveryId)
                .withPublicationTimestamp(LocalDateTime.now())
                .withParticipantRef(validPrefixList.getValidNetexPrefix());
    }
}
