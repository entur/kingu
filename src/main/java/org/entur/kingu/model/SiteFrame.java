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

public class SiteFrame
        extends Common_VersionFrameStructure {

    protected org.entur.kingu.model.TopographicPlacesInFrame topographicPlaces = new org.entur.kingu.model.TopographicPlacesInFrame();
    protected org.entur.kingu.model.StopPlacesInFrame_RelStructure stopPlaces;
    protected org.entur.kingu.model.PointsOfInterestInFrame_RelStructure pointsOfInterest;
    protected org.entur.kingu.model.ParkingsInFrame_RelStructure parkings;
    protected NavigationPathsInFrame_RelStructure navigationPaths;
    protected org.entur.kingu.model.PathLinksInFrame_RelStructure pathLinks;
    protected PathJunctionsInFrame_RelStructure pathJunctions;
    protected CheckConstraintInFrame_RelStructure checkConstraints;
    protected CheckConstraintDelaysInFrame_RelStructure checkConstraintDelays;
    protected org.entur.kingu.model.PointOfInterestClassifications pointOfInterestClassifications;
    protected PointOfInterestClassificationHierarchiesInFrame_RelStructure pointOfInterestClassificationHierarchies;
    protected org.entur.kingu.model.TariffZonesInFrame_RelStructure tariffZones;

    public org.entur.kingu.model.TopographicPlacesInFrame getTopographicPlaces() {
        return topographicPlaces;
    }

    public void setTopographicPlaces(org.entur.kingu.model.TopographicPlacesInFrame value) {
        this.topographicPlaces = value;
    }

    public org.entur.kingu.model.StopPlacesInFrame_RelStructure getStopPlaces() {
        return stopPlaces;
    }

    public void setStopPlaces(org.entur.kingu.model.StopPlacesInFrame_RelStructure value) {
        this.stopPlaces = value;
    }

    public org.entur.kingu.model.PointsOfInterestInFrame_RelStructure getPointsOfInterest() {
        return pointsOfInterest;
    }

    public void setPointsOfInterest(org.entur.kingu.model.PointsOfInterestInFrame_RelStructure value) {
        this.pointsOfInterest = value;
    }

    public org.entur.kingu.model.ParkingsInFrame_RelStructure getParkings() {
        return parkings;
    }

    public void setParkings(org.entur.kingu.model.ParkingsInFrame_RelStructure value) {
        this.parkings = value;
    }

    public NavigationPathsInFrame_RelStructure getNavigationPaths() {
        return navigationPaths;
    }

    public void setNavigationPaths(NavigationPathsInFrame_RelStructure value) {
        this.navigationPaths = value;
    }

    public org.entur.kingu.model.PathLinksInFrame_RelStructure getPathLinks() {
        return pathLinks;
    }

    public void setPathLinks(org.entur.kingu.model.PathLinksInFrame_RelStructure value) {
        this.pathLinks = value;
    }

    public PathJunctionsInFrame_RelStructure getPathJunctions() {
        return pathJunctions;
    }

    public void setPathJunctions(PathJunctionsInFrame_RelStructure value) {
        this.pathJunctions = value;
    }

    public CheckConstraintInFrame_RelStructure getCheckConstraints() {
        return checkConstraints;
    }

    public void setCheckConstraints(CheckConstraintInFrame_RelStructure value) {
        this.checkConstraints = value;
    }

    public CheckConstraintDelaysInFrame_RelStructure getCheckConstraintDelays() {
        return checkConstraintDelays;
    }

    public void setCheckConstraintDelays(CheckConstraintDelaysInFrame_RelStructure value) {
        this.checkConstraintDelays = value;
    }

    public org.entur.kingu.model.PointOfInterestClassifications getPointOfInterestClassifications() {
        return pointOfInterestClassifications;
    }

    public void setPointOfInterestClassifications(org.entur.kingu.model.PointOfInterestClassifications value) {
        this.pointOfInterestClassifications = value;
    }

    public PointOfInterestClassificationHierarchiesInFrame_RelStructure getPointOfInterestClassificationHierarchies() {
        return pointOfInterestClassificationHierarchies;
    }

    public void setPointOfInterestClassificationHierarchies(PointOfInterestClassificationHierarchiesInFrame_RelStructure value) {
        this.pointOfInterestClassificationHierarchies = value;
    }

    public org.entur.kingu.model.TariffZonesInFrame_RelStructure getTariffZones() {
        return tariffZones;
    }

    public void setTariffZones(org.entur.kingu.model.TariffZonesInFrame_RelStructure tariffZones) {
        this.tariffZones = tariffZones;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("name", name)
                .add("topoGraphicPlaces", getTopographicPlaces() != null && getTopographicPlaces().getTopographicPlace() != null ? getTopographicPlaces().getTopographicPlace().size() : 0)
                .add("stops", getStopPlaces() != null && getStopPlaces().getStopPlace() != null ? getStopPlaces().getStopPlace().size() : 0)
                .add("keyValues", getKeyValues())
                .toString();
    }
}
