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

public class PointOfInterest_VersionStructure
        extends org.entur.kingu.model.Site_VersionStructure {

    protected PointOfInterestClassificationsViews_RelStructure classifications;
    protected org.entur.kingu.model.PointOfInterestSpaces_RelStructure spaces;
    protected org.entur.kingu.model.TopographicPlaceRefs_RelStructure nearTopographicPlaces;
    protected org.entur.kingu.model.SitePathLinks_RelStructure pathLinks;
    protected org.entur.kingu.model.PathJunctions_RelStructure pathJunctions;
    protected org.entur.kingu.model.NavigationPaths_RelStructure navigationPaths;

    public PointOfInterestClassificationsViews_RelStructure getClassifications() {
        return classifications;
    }

    public void setClassifications(PointOfInterestClassificationsViews_RelStructure value) {
    }

    public org.entur.kingu.model.PointOfInterestSpaces_RelStructure getSpaces() {
        return spaces;
    }

    public void setSpaces(org.entur.kingu.model.PointOfInterestSpaces_RelStructure value) {
        this.spaces = value;
    }

    public org.entur.kingu.model.TopographicPlaceRefs_RelStructure getNearTopographicPlaces() {
        return nearTopographicPlaces;
    }

    public void setNearTopographicPlaces(org.entur.kingu.model.TopographicPlaceRefs_RelStructure value) {
        this.nearTopographicPlaces = value;
    }

    public org.entur.kingu.model.SitePathLinks_RelStructure getPathLinks() {
        return pathLinks;
    }

    public void setPathLinks(org.entur.kingu.model.SitePathLinks_RelStructure value) {
        this.pathLinks = value;
    }

    public org.entur.kingu.model.PathJunctions_RelStructure getPathJunctions() {
        return pathJunctions;
    }

    public void setPathJunctions(org.entur.kingu.model.PathJunctions_RelStructure value) {
        this.pathJunctions = value;
    }

    public org.entur.kingu.model.NavigationPaths_RelStructure getNavigationPaths() {
        return navigationPaths;
    }

    public void setNavigationPaths(org.entur.kingu.model.NavigationPaths_RelStructure value) {
        this.navigationPaths = value;
    }

}
