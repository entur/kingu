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
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.Parking;
import org.rutebanken.netex.model.ParkingArea;
import org.rutebanken.netex.model.ParkingAreas_RelStructure;

import java.util.List;

public class ParkingMapper extends CustomMapper<Parking, org.entur.kingu.model.Parking> {

    @Override
    public void mapAtoB(Parking parking, org.entur.kingu.model.Parking parking2, MappingContext context) {
        // not implemented - we only map from Kingu to NeTEx
    }

    @Override
    public void mapBtoA(org.entur.kingu.model.Parking tiamatParking, Parking netexParking, MappingContext context) {
        super.mapBtoA(tiamatParking, netexParking, context);
        if (tiamatParking.getParkingAreas() != null &&
                !tiamatParking.getParkingAreas().isEmpty()) {

            List<ParkingArea> parkingAreas = mapperFacade.mapAsList(tiamatParking.getParkingAreas(), ParkingArea.class, context);
            final List<JAXBElement<ParkingArea>> wrappedParkingAreas = parkingAreas.stream()
                    .map(pa -> new ObjectFactory().createParkingArea(pa))
                    .toList();
            if (!parkingAreas.isEmpty()) {
                ParkingAreas_RelStructure parkingAreas_relStructure = new ParkingAreas_RelStructure();
                parkingAreas_relStructure.getParkingAreaRefOrParkingArea_().addAll(wrappedParkingAreas);

                netexParking.setParkingAreas(parkingAreas_relStructure);
            }
        }
    }
}
