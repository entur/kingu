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
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import org.entur.kingu.model.identification.IdentifiedEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PathLinkEnd extends IdentifiedEntity {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "ref", column = @Column(name = "place_ref")),
            @AttributeOverride(name = "version", column = @Column(name = "place_version"))
    })
    private org.entur.kingu.model.AddressablePlaceRefStructure placeRef;

    @Transient
    @OneToOne
    private PointOfInterest pointOfInterest;

    @Transient
    @OneToOne
    private org.entur.kingu.model.AccessSpace accessSpace;

    @ManyToOne
    private PathJunction pathJunction;

    @Transient
    private SiteEntrance entrance;

    @Transient
    private Level level;

    public PathLinkEnd() {
    }

    public PathLinkEnd(org.entur.kingu.model.AddressablePlaceRefStructure placeRef) {
        this.placeRef = placeRef;
    }


    public PathLinkEnd(org.entur.kingu.model.AccessSpace accessSpace) {
        this.accessSpace = accessSpace;
    }

    public PathLinkEnd(SiteEntrance entrance) {
        this.entrance = entrance;
    }

    public PathLinkEnd(PathJunction pathJunction) {
        this.pathJunction = pathJunction;
    }

    public PathLinkEnd(Level level) {
        this.level = level;
    }


    public org.entur.kingu.model.AddressablePlaceRefStructure getPlaceRef() {
        return placeRef;
    }

    public void setPlaceRef(org.entur.kingu.model.AddressablePlaceRefStructure placeRef) {
        this.placeRef = placeRef;
    }

    public SiteEntrance getEntrance() {
        return entrance;
    }

    public void setEntrance(SiteEntrance entrance) {
        this.entrance = entrance;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }


    public PathJunction getPathJunction() {
        return pathJunction;
    }

    public void setPathJunction(PathJunction pathJunction) {
        this.pathJunction = pathJunction;
    }

    public void setPointOfInterest(PointOfInterest pointOfInterest) {
        this.pointOfInterest = pointOfInterest;
    }

    public void setAccessSpace(org.entur.kingu.model.AccessSpace accessSpace) {
        this.accessSpace = accessSpace;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("placeRef", placeRef)
                .add("pathJunction", pathJunction)
                .add("level", level)
                .add("entrace", entrance)
                .toString();
    }

}
