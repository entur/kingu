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

import java.math.BigInteger;


public class NavigationPath_VersionStructure
        extends org.entur.kingu.model.LinkSequence_VersionStructure {

    protected org.entur.kingu.model.PathLinkEnd from;
    protected org.entur.kingu.model.PathLinkEnd to;
    protected org.entur.kingu.model.AccessibilityAssessment accessibilityAssessment;
    protected org.entur.kingu.model.TransferDuration transferDuration;
    protected org.entur.kingu.model.PublicUseEnumeration publicUse;
    protected CoveredEnumeration covered;
    protected org.entur.kingu.model.GatedEnumeration gated;
    protected org.entur.kingu.model.LightingEnumeration lighting;
    protected Boolean allAreasWheelchairAccessible;
    protected BigInteger personCapacity;
    protected org.entur.kingu.model.NavigationTypeEnumeration navigationType;
    protected PlacesInSequence_RelStructure placesInSequence;
    protected org.entur.kingu.model.PathLinksInSequence_RelStructure pathLinksInSequence;

    public org.entur.kingu.model.PathLinkEnd getFrom() {
        return from;
    }

    public void setFrom(org.entur.kingu.model.PathLinkEnd value) {
        this.from = value;
    }

    public org.entur.kingu.model.PathLinkEnd getTo() {
        return to;
    }

    public void setTo(org.entur.kingu.model.PathLinkEnd value) {
        this.to = value;
    }

    public org.entur.kingu.model.AccessibilityAssessment getAccessibilityAssessment() {
        return accessibilityAssessment;
    }

    public void setAccessibilityAssessment(org.entur.kingu.model.AccessibilityAssessment value) {
        this.accessibilityAssessment = value;
    }

    public org.entur.kingu.model.TransferDuration getTransferDuration() {
        return transferDuration;
    }

    public void setTransferDuration(org.entur.kingu.model.TransferDuration value) {
        this.transferDuration = value;
    }

    public org.entur.kingu.model.PublicUseEnumeration getPublicUse() {
        return publicUse;
    }

    public void setPublicUse(org.entur.kingu.model.PublicUseEnumeration value) {
        this.publicUse = value;
    }

    public CoveredEnumeration getCovered() {
        return covered;
    }

    public void setCovered(CoveredEnumeration value) {
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

    public org.entur.kingu.model.NavigationTypeEnumeration getNavigationType() {
        return navigationType;
    }

    public void setNavigationType(org.entur.kingu.model.NavigationTypeEnumeration value) {
        this.navigationType = value;
    }

    public PlacesInSequence_RelStructure getPlacesInSequence() {
        return placesInSequence;
    }

    public void setPlacesInSequence(PlacesInSequence_RelStructure value) {
        this.placesInSequence = value;
    }

    public org.entur.kingu.model.PathLinksInSequence_RelStructure getPathLinksInSequence() {
        return pathLinksInSequence;
    }

    public void setPathLinksInSequence(org.entur.kingu.model.PathLinksInSequence_RelStructure value) {
        this.pathLinksInSequence = value;
    }

}
