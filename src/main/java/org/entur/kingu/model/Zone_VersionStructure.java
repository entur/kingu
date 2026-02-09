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

package org.entur.kingu.model;

import com.google.common.base.MoreObjects;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;


@MappedSuperclass
public class Zone_VersionStructure
        extends org.entur.kingu.model.GroupOfPoints_VersionStructure {

    protected Point centroid;

    /**
     * Polygon is wrapped in PersistablePolygon.
     * Because we want to fetch polygons lazily and using lazy property fetching with byte code enhancement breaks tests.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    protected org.entur.kingu.model.PersistablePolygon polygon;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    protected org.entur.kingu.model.PersistableMultiPolygon multiSurface;

    @Transient
    protected org.entur.kingu.model.Projections_RelStructure projections;

    @Transient
    protected org.entur.kingu.model.ZoneRefStructure parentZoneRef;

    public Zone_VersionStructure() {
    }

    public Zone_VersionStructure(org.entur.kingu.model.EmbeddableMultilingualString name) {
        super(name);
    }

    public Polygon getPolygon() {
        if (polygon != null) {
            return polygon.getPolygon();
        }
        return null;
    }

    public void setPolygon(Polygon polygon) {
        if (polygon == null) {
            this.polygon = null;
            return;
        }
        if (this.polygon == null) {
            this.polygon = new org.entur.kingu.model.PersistablePolygon();
        }
        this.polygon.setPolygon(polygon);
    }

    public MultiPolygon getMultiSurface() {
        if (multiSurface != null) {
            return multiSurface.getMultiPolygon();
        }
        return null;
    }

    public void setMultiSurface(MultiPolygon multiPolygon) {
        if (multiPolygon == null) {
            this.multiSurface = null;
            return;
        }
        if (this.multiSurface == null) {
            this.multiSurface = new org.entur.kingu.model.PersistableMultiPolygon();
        }
        this.multiSurface.setMultiPolygon(multiPolygon);
    }

    public Point  getCentroid() {
        return centroid;
    }

    public void setCentroid(Point value) {
        this.centroid = value;
    }

    public org.entur.kingu.model.Projections_RelStructure getProjections() {
        return projections;
    }

    public void setProjections(org.entur.kingu.model.Projections_RelStructure value) {
        this.projections = value;
    }

    public org.entur.kingu.model.ZoneRefStructure getParentZoneRef() {
        return parentZoneRef;
    }

    public void setParentZoneRef(org.entur.kingu.model.ZoneRefStructure value) {
        this.parentZoneRef = value;
    }

    public boolean hasCoordinates() {
        return centroid != null;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("netexId", netexId)
                .add("version", version)
                .add("name", name)
                .toString();
    }
}
