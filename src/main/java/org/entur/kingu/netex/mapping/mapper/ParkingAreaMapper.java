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
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import org.entur.kingu.model.ParkingProperties;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.ParkingArea;
import org.entur.netex2.model.ParkingProperties_RelStructure;

import java.util.List;

/**
 * The generated netex {@link ParkingArea} has no typed properties for its own elements
 * (TotalCapacity, ParkingProperties, bays, entrances): because the schema uses the name
 * "ParkingProperties" twice, xjc puts them all in a catch-all {@code rest} list of JAXBElements.
 * Orika cannot map into that by convention, so the elements are added here explicitly. They are
 * added in schema order (TotalCapacity before parkingProperties), which JAXB keeps when marshalling.
 */
public class ParkingAreaMapper extends CustomMapper<ParkingArea, org.entur.kingu.model.ParkingArea> {

    private static final ObjectFactory objectFactory = new ObjectFactory();
    private static final Type<List<ParkingProperties>> PARKING_PROPERTIES_LIST =
            new TypeBuilder<List<ParkingProperties>>() {}.build();
    private static final Type<ParkingProperties_RelStructure> PARKING_PROPERTIES_REL =
            TypeFactory.valueOf(ParkingProperties_RelStructure.class);

    @Override
    public void mapAtoB(ParkingArea netexParkingArea, org.entur.kingu.model.ParkingArea parkingArea, MappingContext context) {
        // not implemented - we only map from Kingu to NeTEx
    }

    @Override
    public void mapBtoA(org.entur.kingu.model.ParkingArea parkingArea, ParkingArea netexParkingArea, MappingContext context) {
        super.mapBtoA(parkingArea, netexParkingArea, context);

        if (parkingArea.getTotalCapacity() != null) {
            netexParkingArea.getRest().add(
                    objectFactory.createParkingArea_VersionStructureTotalCapacity(parkingArea.getTotalCapacity()));
        }

        if (parkingArea.getParkingProperties() != null) {
            // Typed source so Orika picks ParkingPropertiesListConverter, as it does for Parking.
            ParkingProperties_RelStructure parkingProperties = mapperFacade.map(
                    List.of(parkingArea.getParkingProperties()), PARKING_PROPERTIES_LIST, PARKING_PROPERTIES_REL, context);
            if (parkingProperties != null) {
                netexParkingArea.getRest().add(
                        objectFactory.createParkingArea_VersionStructureParkingProperties(parkingProperties));
            }
        }
    }
}
