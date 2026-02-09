package org.entur.kingu.netex.mapping.converter;

import net.opengis.gml._3.MultiSurfaceType;
import net.opengis.gml._3.PolygonType;
import net.opengis.gml._3.SurfacePropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MultiSurfaceConverterTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();
    private MultiSurfaceConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MultiSurfaceConverter();
    }

    @Test
    void convertToMultiSurfaceTypeWithTwoPolygons() {
        Polygon polygon1 = createJtsPolygon(10.0, 59.0, 10.0, 59.1, 10.1, 59.1, 10.1, 59.0, 10.0, 59.0);
        Polygon polygon2 = createJtsPolygon(11.0, 60.0, 11.0, 60.1, 11.1, 60.1, 11.1, 60.0, 11.0, 60.0);
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{polygon1, polygon2});

        MultiSurfaceType result = converter.convertTo(multiPolygon, null, null);

        assertNotNull(result);
        assertNotNull(result.getSurfaceMember());
        assertEquals(2, result.getSurfaceMember().size());

        for (SurfacePropertyType surfaceProperty : result.getSurfaceMember()) {
            assertNotNull(surfaceProperty.getAbstractSurface());
            PolygonType polygonType = (PolygonType) surfaceProperty.getAbstractSurface().getValue();
            assertNotNull(polygonType.getExterior());
        }
    }

    @Test
    void convertToNullReturnsNull() {
        assertNull(converter.convertTo(null, null, null));
    }

    @Test
    void convertToEmptyMultiPolygonReturnsNull() {
        MultiPolygon emptyMultiPolygon = geometryFactory.createMultiPolygon(new Polygon[0]);
        assertNull(converter.convertTo(emptyMultiPolygon, null, null));
    }


    @Test
    void convertToPolygonWithInteriorRing() {
        LinearRing exteriorRing = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(10.0, 59.0),
                new Coordinate(10.0, 59.1),
                new Coordinate(10.1, 59.1),
                new Coordinate(10.1, 59.0),
                new Coordinate(10.0, 59.0)
        });
        LinearRing interiorRing = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(10.03, 59.03),
                new Coordinate(10.03, 59.07),
                new Coordinate(10.07, 59.07),
                new Coordinate(10.07, 59.03),
                new Coordinate(10.03, 59.03)
        });
        Polygon polygon = geometryFactory.createPolygon(exteriorRing, new LinearRing[]{interiorRing});
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{polygon});

        MultiSurfaceType result = converter.convertTo(multiPolygon, null, null);

        assertNotNull(result);
        assertEquals(1, result.getSurfaceMember().size());
        PolygonType polygonType = (PolygonType) result.getSurfaceMember().get(0).getAbstractSurface().getValue();
        assertNotNull(polygonType.getExterior());
        assertEquals(1, polygonType.getInterior().size());
    }


    private Polygon createJtsPolygon(double... xyPairs) {
        Coordinate[] coords = new Coordinate[xyPairs.length / 2];
        for (int i = 0; i < xyPairs.length; i += 2) {
            coords[i / 2] = new Coordinate(xyPairs[i], xyPairs[i + 1]);
        }
        return geometryFactory.createPolygon(coords);
    }
}
