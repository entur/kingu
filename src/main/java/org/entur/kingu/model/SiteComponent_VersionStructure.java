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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.List;


@MappedSuperclass
public abstract class SiteComponent_VersionStructure
        extends org.entur.kingu.model.SiteElement {

    @AttributeOverrides({
            @AttributeOverride(name = "ref", column = @Column(name = "site_ref")),
            @AttributeOverride(name = "version", column = @Column(name = "site_ref_version"))
    })
    @Embedded
    protected org.entur.kingu.model.SiteRefStructure siteRef;

    @AttributeOverrides({
            @AttributeOverride(name = "ref", column = @Column(name = "level_ref")),
            @AttributeOverride(name = "version", column = @Column(name = "level_ref_version"))
    })
    @Embedded
    protected LevelRefStructure levelRef;

    @Transient
    protected org.entur.kingu.model.ClassOfUseRef classOfUseRef;
    @OneToMany(cascade = CascadeType.ALL)
    protected List<org.entur.kingu.model.EquipmentPlace> equipmentPlaces;
    @OneToOne(cascade = CascadeType.ALL)
    protected PlaceEquipment placeEquipments;

    @OneToMany(cascade = CascadeType.ALL)
    private List<CheckConstraint> checkConstraints;

    public SiteComponent_VersionStructure(org.entur.kingu.model.EmbeddableMultilingualString name) {
        super(name);
    }

    protected SiteComponent_VersionStructure() {
    }


    public org.entur.kingu.model.SiteRefStructure getSiteRef() {
        return siteRef;
    }


    public void setSiteRef(org.entur.kingu.model.SiteRefStructure value) {
        this.siteRef = value;
    }


    public LevelRefStructure getLevelRef() {
        return levelRef;
    }


    public void setLevelRef(LevelRefStructure value) {
        this.levelRef = value;
    }


    public org.entur.kingu.model.ClassOfUseRef getClassOfUseRef() {
        return classOfUseRef;
    }


    public void setClassOfUseRef(org.entur.kingu.model.ClassOfUseRef value) {
    }


    public List<org.entur.kingu.model.EquipmentPlace> getEquipmentPlaces() {
        return equipmentPlaces;
    }


    public void setEquipmentPlaces(List<org.entur.kingu.model.EquipmentPlace> value) {
        this.equipmentPlaces = value;
    }


    public PlaceEquipment getPlaceEquipments() {
        return placeEquipments;
    }


    public void setPlaceEquipments(PlaceEquipment value) {
        this.placeEquipments = value;
    }

    public List<CheckConstraint> getCheckConstraints() {
        return checkConstraints;
    }

    public void setCheckConstraints(List<CheckConstraint> checkConstraints) {
        this.checkConstraints = checkConstraints;
    }
}
