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

package org.entur.kingu.netex.mapping.converter;


import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import net.opengis.gml._3.AbstractRingPropertyType;
import net.opengis.gml._3.DirectPositionListType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.MultiSurfaceType;
import net.opengis.gml._3.ObjectFactory;
import net.opengis.gml._3.PolygonType;
import net.opengis.gml._3.SurfacePropertyType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class MultiSurfaceConverter extends BidirectionalConverter<MultiPolygon, MultiSurfaceType> {

    private static final ObjectFactory openGisObjectFactory = new ObjectFactory();

    private static final AtomicLong multiSurfaceIdCounter = new AtomicLong();
    private static final AtomicLong polygonIdCounter = new AtomicLong();

    @Autowired
    public MultiSurfaceConverter() {
    }

    @Override
    public MultiPolygon convertFrom(MultiSurfaceType multiSurfaceType, Type<MultiPolygon> type, MappingContext mappingContext) {
        // not implemented in this direction, as we only use this converter for converting from JTS to Netex
        return null;
    }

    @Override
    public MultiSurfaceType convertTo(MultiPolygon multiPolygon, Type<MultiSurfaceType> type, MappingContext mappingContext) {
        if (multiPolygon == null || multiPolygon.getNumGeometries() == 0) {
            return null;
        }

        MultiSurfaceType multiSurfaceType = new MultiSurfaceType()
                .withId("GEN-MultiSurfaceType-" + multiSurfaceIdCounter.incrementAndGet());

        List<SurfacePropertyType> surfaceMembers = new ArrayList<>();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
            PolygonType polygonType = convertToPolygonType(polygon);
            if (polygonType != null) {
                SurfacePropertyType surfaceProperty = new SurfacePropertyType()
                        .withAbstractSurface(openGisObjectFactory.createPolygon(polygonType));
                surfaceMembers.add(surfaceProperty);
            }
        }

        if (surfaceMembers.isEmpty()) {
            return null;
        }

        multiSurfaceType.withSurfaceMember(surfaceMembers);
        return multiSurfaceType;
    }

    private PolygonType convertToPolygonType(Polygon polygon) {
        Optional<Coordinate[]> optionalCoordinates = Optional.ofNullable(polygon)
                .map(Polygon::getExteriorRing)
                .map(LineString::getCoordinates)
                .filter(coordinates -> coordinates.length > 0);

        if (optionalCoordinates.isPresent()) {
            List<Double> values = toList(optionalCoordinates.get());
            return new PolygonType()
                    .withId("GEN-PolygonType-" + polygonIdCounter.incrementAndGet())
                    .withExterior(of(values))
                    .withInterior(ofInteriorRings(polygon));
        }

        return null;
    }

    private List<AbstractRingPropertyType> ofInteriorRings(Polygon polygon) {
        List<AbstractRingPropertyType> list = new ArrayList<>();
        for (int n = 0; n < polygon.getNumInteriorRing(); n++) {
            if (polygon.getInteriorRingN(n).getCoordinates() != null) {
                List<Double> values = toList(polygon.getInteriorRingN(n).getCoordinates());
                list.add(of(values));
            }
        }
        return list;
    }

    private AbstractRingPropertyType of(List<Double> values) {
        return new AbstractRingPropertyType()
                .withAbstractRing(openGisObjectFactory.createLinearRing(
                        new LinearRingType()
                                .withPosList(
                                        new DirectPositionListType().withValue(values))));
    }

    private List<Double> toList(Coordinate[] coordinates) {
        List<Double> values = new ArrayList<>(coordinates.length * 2);
        for (Coordinate coordinate : coordinates) {
            values.add(coordinate.y);
            values.add(coordinate.x);
        }
        return values;
    }
}
