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

package org.entur.kingu.netex.mapping;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.entur.kingu.netex.mapping.mapper.AccessibilityAssessmentMapper;
import org.entur.kingu.netex.mapping.mapper.DataManagedObjectStructureMapper;
import org.entur.kingu.netex.mapping.mapper.FareZoneMapper;
import org.entur.kingu.netex.mapping.mapper.GroupOfTariffZonesMapper;
import org.entur.kingu.netex.mapping.mapper.KeyListToKeyValuesMapMapper;
import org.entur.kingu.netex.mapping.mapper.ParkingMapper;
import org.entur.kingu.netex.mapping.mapper.PlaceEquipmentMapper;
import org.entur.kingu.netex.mapping.mapper.QuayMapper;
import org.entur.kingu.netex.mapping.mapper.StopPlaceMapper;
import org.rutebanken.netex.model.AccessibilityAssessment;
import org.rutebanken.netex.model.CycleStorageEquipment;
import org.rutebanken.netex.model.DataManagedObjectStructure;
import org.rutebanken.netex.model.FareFrame;
import org.rutebanken.netex.model.FareZone;
import org.rutebanken.netex.model.GeneralSign;
import org.rutebanken.netex.model.GroupOfStopPlaces;
import org.rutebanken.netex.model.GroupOfTariffZones;
import org.rutebanken.netex.model.InstalledEquipment_VersionStructure;
import org.rutebanken.netex.model.Parking;
import org.rutebanken.netex.model.PathLink;
import org.rutebanken.netex.model.PathLinkEndStructure;
import org.rutebanken.netex.model.PlaceEquipments_RelStructure;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.SanitaryEquipment;
import org.rutebanken.netex.model.ServiceFrame;
import org.rutebanken.netex.model.ShelterEquipment;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopPlace;
import org.rutebanken.netex.model.TariffZone;
import org.rutebanken.netex.model.TicketingEquipment;
import org.rutebanken.netex.model.TopographicPlace;
import org.rutebanken.netex.model.WaitingRoomEquipment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NetexMapper {
    private static final Logger logger = LoggerFactory.getLogger(NetexMapper.class);
    private final MapperFacade facade;

    @Autowired
    public NetexMapper(List<Converter> converters, KeyListToKeyValuesMapMapper keyListToKeyValuesMapMapper,
                       DataManagedObjectStructureMapper dataManagedObjectStructureMapper,
                       PublicationDeliveryHelper publicationDeliveryHelper,
                       AccessibilityAssessmentMapper accessibilityAssessmentMapper) {

        logger.info("Setting up netexMapper with DI");

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        logger.info("Creating netex mapperFacade with {} converters ", converters.size());

        if(logger.isDebugEnabled()) {
            logger.debug("Converters: {}", converters);
        }

        converters.forEach(converter -> mapperFactory.getConverterFactory().registerConverter(converter));

        // Issues with registering multiple mappers
        mapperFactory.registerMapper(keyListToKeyValuesMapMapper);

        mapperFactory.classMap(SiteFrame.class, org.entur.kingu.model.SiteFrame.class)
                .byDefault()
                .register();

        mapperFactory.classMap(TopographicPlace.class, org.entur.kingu.model.TopographicPlace.class)
                .fieldBToA("name", "descriptor.name")
                .byDefault()
                .register();

        mapperFactory.classMap(GroupOfStopPlaces.class, org.entur.kingu.model.GroupOfStopPlaces.class)
                .byDefault()
                .register();

        mapperFactory.classMap(GroupOfTariffZones.class, org.entur.kingu.model.GroupOfTariffZones.class)
                .byDefault()
                .customize(new GroupOfTariffZonesMapper())
                .register();


        mapperFactory.classMap(StopPlace.class, org.entur.kingu.model.StopPlace.class)
                .fieldBToA("topographicPlace", "topographicPlaceRef")
                .fieldAToB("topographicPlaceRef.ref", "topographicPlace.netexId")
                .fieldAToB("topographicPlaceRef.version", "topographicPlace.version")
                .exclude("localServices")
                .exclude("postalAddress")
                .exclude("roadAddress")
                .customize(new StopPlaceMapper(publicationDeliveryHelper))
                .byDefault()
                .register();

        mapperFactory.classMap(Quay.class, org.entur.kingu.model.Quay.class)
                .exclude("localServices")
                .exclude("postalAddress")
                .exclude("roadAddress")
                .customize(new QuayMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(TariffZone.class, org.entur.kingu.model.TariffZone.class)
                .byDefault()
                .register();

        mapperFactory.classMap(FareZone.class, org.entur.kingu.model.FareZone.class)
                .exclude("transportOrganisationRef")
                .exclude("neighbours")
                .exclude("members")
                .customize(new FareZoneMapper())
                .byDefault()
                .register();


        mapperFactory.classMap(Parking.class, org.entur.kingu.model.Parking.class)
                .exclude("paymentMethods")
                .exclude("cardsAccepted")
                .exclude("currenciesAccepted")
                .exclude("accessModes")
                .fieldBToA("netexId", "id")
                .customize(new ParkingMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(PathLinkEndStructure.class, org.entur.kingu.model.PathLinkEnd.class)
                .byDefault()
                .register();

        mapperFactory.classMap(PathLink.class, org.entur.kingu.model.PathLink.class)
                .byDefault()
                .register();

        mapperFactory.classMap(InstalledEquipment_VersionStructure.class, org.entur.kingu.model.InstalledEquipment_VersionStructure.class)
                .fieldBToA("netexId", "id")
                .byDefault()
                .register();

        mapperFactory.classMap(WaitingRoomEquipment.class, org.entur.kingu.model.WaitingRoomEquipment.class)
                .byDefault()
                .register();

        mapperFactory.classMap(SanitaryEquipment.class, org.entur.kingu.model.SanitaryEquipment.class)
                .byDefault()
                .register();

        mapperFactory.classMap(TicketingEquipment.class, org.entur.kingu.model.TicketingEquipment.class)
                .byDefault()
                .register();

        mapperFactory.classMap(ShelterEquipment.class, org.entur.kingu.model.ShelterEquipment.class)
                .byDefault()
                .register();

        mapperFactory.classMap(CycleStorageEquipment.class, org.entur.kingu.model.CycleStorageEquipment.class)
                .byDefault()
                .register();

        mapperFactory.classMap(GeneralSign.class, org.entur.kingu.model.GeneralSign.class)
                .byDefault()
                .register();

        mapperFactory.classMap(PlaceEquipments_RelStructure.class, org.entur.kingu.model.PlaceEquipment.class)
                .fieldBToA("netexId", "id")
                .customize(new PlaceEquipmentMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(AccessibilityAssessment.class, org.entur.kingu.model.AccessibilityAssessment.class)
                .customize(accessibilityAssessmentMapper)
                .exclude("id")
                .byDefault()
                .register();

        mapperFactory.classMap(DataManagedObjectStructure.class, org.entur.kingu.model.DataManagedObjectStructure.class)
                .fieldBToA("keyValues", "keyList")
                .field("validBetween[0]", "validBetween")
                .customize(dataManagedObjectStructureMapper)
                .exclude("id")
                .exclude("keyList")
                .exclude("keyValues")
                .exclude("version")
                .byDefault()
                .register();

        facade = mapperFactory.getMapperFacade();
    }

    public TopographicPlace mapToNetexModel(org.entur.kingu.model.TopographicPlace topographicPlace) {
        return facade.map(topographicPlace, TopographicPlace.class);
    }

    public TariffZone mapToNetexModel(org.entur.kingu.model.TariffZone tariffZone) {
        return facade.map(tariffZone, TariffZone.class);
    }

    public FareZone mapToNetexModel(org.entur.kingu.model.FareZone fareZone) {
        return facade.map(fareZone, FareZone.class);
    }

    public SiteFrame mapToNetexModel(org.entur.kingu.model.SiteFrame tiamatSiteFrame) {
        return facade.map(tiamatSiteFrame, SiteFrame.class);
    }

    public ServiceFrame mapToNetexModel(org.entur.kingu.model.ServiceFrame tiamatServiceFrame) {
        ServiceFrame serviceFrame = facade.map(tiamatServiceFrame, ServiceFrame.class);
        return serviceFrame;
    }

    public FareFrame mapToNetexModel(org.entur.kingu.model.FareFrame tiamatFareFrame) {
        FareFrame fareFrame = facade.map(tiamatFareFrame, FareFrame.class);
        return fareFrame;
    }

    public StopPlace mapToNetexModel(org.entur.kingu.model.StopPlace tiamatStopPlace) {
        return facade.map(tiamatStopPlace, StopPlace.class);
    }


    public Parking mapToNetexModel(org.entur.kingu.model.Parking tiamatParking) {
        return facade.map(tiamatParking, Parking.class);
    }

    public org.entur.kingu.model.TopographicPlace mapToTiamatModel(TopographicPlace topographicPlace) {
        return facade.map(topographicPlace, org.entur.kingu.model.TopographicPlace.class);
    }

    public org.entur.kingu.model.TariffZone mapToTiamatModel(TariffZone tariffZone) {
        return facade.map(tariffZone, org.entur.kingu.model.TariffZone.class);
    }

    public org.entur.kingu.model.FareZone mapToTiamatModel(FareZone fareZone) {
        return facade.map(fareZone,org.entur.kingu.model.FareZone.class);
    }

    public List<org.entur.kingu.model.StopPlace> mapStopsToTiamatModel(List<StopPlace> stopPlaces) {
        return facade.mapAsList(stopPlaces, org.entur.kingu.model.StopPlace.class);
    }

    public List<org.entur.kingu.model.Parking> mapParkingsToTiamatModel(List<Parking> parking) {
        return facade.mapAsList(parking, org.entur.kingu.model.Parking.class);
    }

    public List<org.entur.kingu.model.PathLink> mapPathLinksToTiamatModel(List<PathLink> pathLinks) {
        return facade.mapAsList(pathLinks, org.entur.kingu.model.PathLink.class);
    }

    public org.entur.kingu.model.SiteFrame mapToTiamatModel(SiteFrame netexSiteFrame) {
        org.entur.kingu.model.SiteFrame tiamatSiteFrame = facade.map(netexSiteFrame, org.entur.kingu.model.SiteFrame.class);
        return tiamatSiteFrame;
    }

    public org.entur.kingu.model.StopPlace mapToTiamatModel(StopPlace netexStopPlace) {
        return facade.map(netexStopPlace, org.entur.kingu.model.StopPlace.class);
    }

    public org.entur.kingu.model.Quay mapToTiamatModel(Quay netexQuay) {
        return facade.map(netexQuay, org.entur.kingu.model.Quay.class);
    }


    public org.entur.kingu.model.Parking mapToTiamatModel(Parking netexParking) {
        return facade.map(netexParking, org.entur.kingu.model.Parking.class);
    }

    public Quay mapToNetexModel(org.entur.kingu.model.Quay tiamatQuay) {
        return facade.map(tiamatQuay, Quay.class);
    }

    public PathLink mapToNetexModel(org.entur.kingu.model.PathLink pathLink) {
        return facade.map(pathLink, PathLink.class);
    }

    public MapperFacade getFacade() {
        return facade;
    }

    public org.entur.kingu.model.GroupOfTariffZones mapToTiamatModel(GroupOfTariffZones netexGroupOfTariffZones) {
        final org.entur.kingu.model.GroupOfTariffZones groupOfTariffZones = facade.map(netexGroupOfTariffZones, org.entur.kingu.model.GroupOfTariffZones.class);
        return groupOfTariffZones;
    }
}
