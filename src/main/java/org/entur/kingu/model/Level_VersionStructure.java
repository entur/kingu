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

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.JAXBElement;


@MappedSuperclass
public class Level_VersionStructure
        extends org.entur.kingu.model.DataManagedObjectStructure {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected org.entur.kingu.model.MultilingualStringEntity name;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected org.entur.kingu.model.MultilingualStringEntity shortName;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected org.entur.kingu.model.MultilingualStringEntity description;

    protected String publicCode;

    protected Boolean publicUse;

    @Transient
    protected org.entur.kingu.model.AccessibilityAssessment_VersionedChildStructure accessibilityAssessment;

    protected Boolean allAreasWheelchairAccessible;

    @Transient
    protected JAXBElement<? extends org.entur.kingu.model.SiteRefStructure> siteRef;

    public org.entur.kingu.model.MultilingualStringEntity getName() {
        return name;
    }

    public void setName(org.entur.kingu.model.MultilingualStringEntity value) {
        this.name = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getShortName() {
        return shortName;
    }

    public void setShortName(org.entur.kingu.model.MultilingualStringEntity value) {
        this.shortName = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getDescription() {
        return description;
    }

    public void setDescription(org.entur.kingu.model.MultilingualStringEntity value) {
        this.description = value;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(String value) {
        this.publicCode = value;
    }

    public Boolean isPublicUse() {
        return publicUse;
    }

    public void setPublicUse(Boolean value) {
        this.publicUse = value;
    }

    public org.entur.kingu.model.AccessibilityAssessment_VersionedChildStructure getAccessibilityAssessment() {
        return accessibilityAssessment;
    }

    public void setAccessibilityAssessment(org.entur.kingu.model.AccessibilityAssessment_VersionedChildStructure value) {
        this.accessibilityAssessment = value;
    }

    public Boolean isAllAreasWheelchairAccessible() {
        return allAreasWheelchairAccessible;
    }

    public void setAllAreasWheelchairAccessible(Boolean value) {
        this.allAreasWheelchairAccessible = value;
    }

    public JAXBElement<? extends org.entur.kingu.model.SiteRefStructure> getSiteRef() {
        return siteRef;
    }

    public void setSiteRef(JAXBElement<? extends org.entur.kingu.model.SiteRefStructure> value) {
        this.siteRef = value;
    }

}
