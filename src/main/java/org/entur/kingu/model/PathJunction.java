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

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigInteger;

@Entity
public class PathJunction extends org.entur.kingu.model.Point {

    @Transient
    protected org.entur.kingu.model.ZoneRefStructure parentZoneRef;

    @Transient
    protected org.entur.kingu.model.PublicUseEnumeration publicUse;

    @Transient
    protected org.entur.kingu.model.CoveredEnumeration covered;

    @Transient
    protected org.entur.kingu.model.GatedEnumeration gated;

    @Transient
    protected org.entur.kingu.model.LightingEnumeration lighting;

    @Transient
    protected Boolean allAreasWheelchairAccessible;

    @Transient
    protected BigInteger personCapacity;

    @Transient
    protected org.entur.kingu.model.MultilingualStringEntity label;

    @Transient
    protected org.entur.kingu.model.SiteComponentRefStructure siteComponentRef;

    public org.entur.kingu.model.ZoneRefStructure getParentZoneRef() {
        return parentZoneRef;
    }

    public void setParentZoneRef(org.entur.kingu.model.ZoneRefStructure value) {
        this.parentZoneRef = value;
    }

    public org.entur.kingu.model.PublicUseEnumeration getPublicUse() {
        return publicUse;
    }

    public void setPublicUse(org.entur.kingu.model.PublicUseEnumeration value) {
        this.publicUse = value;
    }

    public org.entur.kingu.model.CoveredEnumeration getCovered() {
        return covered;
    }

    public void setCovered(org.entur.kingu.model.CoveredEnumeration value) {
        this.covered = value;
    }

    public org.entur.kingu.model.GatedEnumeration getGated() {
        return gated;
    }

    public void setGated(org.entur.kingu.model.GatedEnumeration value) {
        this.gated = value;
    }

    public org.entur.kingu.model.LightingEnumeration getLighting() {
        return lighting;
    }

    public void setLighting(org.entur.kingu.model.LightingEnumeration value) {
        this.lighting = value;
    }

    public Boolean isAllAreasWheelchairAccessible() {
        return allAreasWheelchairAccessible;
    }

    public void setAllAreasWheelchairAccessible(Boolean value) {
        this.allAreasWheelchairAccessible = value;
    }

    public BigInteger getPersonCapacity() {
        return personCapacity;
    }

    public void setPersonCapacity(BigInteger value) {
        this.personCapacity = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getLabel() {
        return label;
    }

    public void setLabel(org.entur.kingu.model.MultilingualStringEntity value) {
        this.label = value;
    }

    public org.entur.kingu.model.SiteComponentRefStructure getSiteComponentRef() {
        return siteComponentRef;
    }

    public void setSiteComponentRef(org.entur.kingu.model.SiteComponentRefStructure value) {
        this.siteComponentRef = value;
    }

}
