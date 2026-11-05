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

package org.entur.kingu.netex.mapping.mapper;

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.entur.kingu.model.PlaceEquipment;
import org.entur.netex2.model.CycleStorageEquipment;
import org.entur.netex2.model.GeneralSign;
import org.entur.netex2.model.InstalledEquipment_VersionStructure;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.PlaceEquipments_RelStructure;
import org.entur.netex2.model.SanitaryEquipment;
import org.entur.netex2.model.ShelterEquipment;
import org.entur.netex2.model.TicketingEquipment;
import org.entur.netex2.model.WaitingRoomEquipment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaceEquipmentMapper extends CustomMapper<PlaceEquipments_RelStructure, PlaceEquipment> {

    final ObjectFactory objectFactory = new ObjectFactory();


    @Override
    public void mapAtoB(PlaceEquipments_RelStructure placeEquipments_relStructure, org.entur.kingu.model.PlaceEquipment placeEquipment, MappingContext context) {

        super.mapAtoB(placeEquipments_relStructure, placeEquipment, context);

        List<InstalledEquipment_VersionStructure> netexInstalledEquipmentList = placeEquipments_relStructure
                .getInstalledEquipmentRefOrInstalledEquipment()
                .stream()
                .filter(jaxbElement -> {
                    Object equipment = jaxbElement.getValue();
                    return (equipment instanceof SanitaryEquipment ||
                            equipment instanceof TicketingEquipment ||
                            equipment instanceof WaitingRoomEquipment ||
                            equipment instanceof CycleStorageEquipment ||
                            equipment instanceof ShelterEquipment ||
                            equipment instanceof GeneralSign);
                })
                .map(jaxbElement -> (InstalledEquipment_VersionStructure)jaxbElement.getValue())
                .toList();
        List<org.entur.kingu.model.InstalledEquipment_VersionStructure> installedEquipment_versionStructures = mapperFacade.mapAsList(netexInstalledEquipmentList, org.entur.kingu.model.InstalledEquipment_VersionStructure.class, context);

        if (!installedEquipment_versionStructures.isEmpty()) {
            placeEquipment.getInstalledEquipment().addAll(installedEquipment_versionStructures);
        }
    }

    @Override
    public void mapBtoA(org.entur.kingu.model.PlaceEquipment placeEquipment, PlaceEquipments_RelStructure placeEquipments_relStructure, MappingContext context) {
        mapperFacade.map(placeEquipment, placeEquipments_relStructure, context);

        List<InstalledEquipment_VersionStructure> installedEquipment_versionStructures = mapperFacade.mapAsList(placeEquipment.getInstalledEquipment(), InstalledEquipment_VersionStructure.class, context);

        List<JAXBElement<? extends InstalledEquipment_VersionStructure>> jaxbElements = installedEquipment_versionStructures
                .stream()
                .filter(equipment -> (equipment instanceof SanitaryEquipment ||
                        equipment instanceof TicketingEquipment ||
                        equipment instanceof WaitingRoomEquipment ||
                        equipment instanceof CycleStorageEquipment ||
                        equipment instanceof ShelterEquipment ||
                        equipment instanceof GeneralSign))
                .map(equipment -> {
                    if (equipment instanceof SanitaryEquipment) {
                        return objectFactory.createSanitaryEquipment((SanitaryEquipment) equipment);
                    } else if (equipment instanceof TicketingEquipment) {
                        return objectFactory.createTicketingEquipment((TicketingEquipment) equipment);
                    } else if (equipment instanceof WaitingRoomEquipment) {
                        return objectFactory.createWaitingRoomEquipment((WaitingRoomEquipment) equipment);
                    } else if (equipment instanceof CycleStorageEquipment) {
                        return objectFactory.createCycleStorageEquipment((CycleStorageEquipment) equipment);
                    } else if (equipment instanceof ShelterEquipment) {
                        return objectFactory.createShelterEquipment((ShelterEquipment) equipment);
                    } else if (equipment instanceof GeneralSign) {
                        return objectFactory.createGeneralSign((GeneralSign) equipment);
                    }
                    return null;
                })
                .toList();


        if (!jaxbElements.isEmpty()) {
            placeEquipments_relStructure.getInstalledEquipmentRefOrInstalledEquipment().addAll(jaxbElements);
        }


    }
}
