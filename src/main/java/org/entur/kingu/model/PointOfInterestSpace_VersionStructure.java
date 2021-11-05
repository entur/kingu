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

public class PointOfInterestSpace_VersionStructure
        extends org.entur.kingu.model.PointOfInterestComponent_VersionStructure {

    protected org.entur.kingu.model.AccessSpaceTypeEnumeration accessSpaceType;
    protected PointOfInterestSpaceTypeEnumeration pointOfInterestSpaceType;
    protected PassageTypeEnumeration passageType;
    protected org.entur.kingu.model.PointOfInterestSpaceRefStructure parentPointOfInterestSpaceRef;
    protected PointOfInterestEntrances_RelStructure entrances;

    public org.entur.kingu.model.AccessSpaceTypeEnumeration getAccessSpaceType() {
        return accessSpaceType;
    }

    public void setAccessSpaceType(org.entur.kingu.model.AccessSpaceTypeEnumeration value) {
        this.accessSpaceType = value;
    }

    public PointOfInterestSpaceTypeEnumeration getPointOfInterestSpaceType() {
        return pointOfInterestSpaceType;
    }

    public void setPointOfInterestSpaceType(PointOfInterestSpaceTypeEnumeration value) {
        this.pointOfInterestSpaceType = value;
    }

    public PassageTypeEnumeration getPassageType() {
        return passageType;
    }

    public void setPassageType(PassageTypeEnumeration value) {
        this.passageType = value;
    }

    public org.entur.kingu.model.PointOfInterestSpaceRefStructure getParentPointOfInterestSpaceRef() {
        return parentPointOfInterestSpaceRef;
    }

    public void setParentPointOfInterestSpaceRef(org.entur.kingu.model.PointOfInterestSpaceRefStructure value) {
        this.parentPointOfInterestSpaceRef = value;
    }

    public PointOfInterestEntrances_RelStructure getEntrances() {
        return entrances;
    }

    public void setEntrances(PointOfInterestEntrances_RelStructure value) {
        this.entrances = value;
    }

}
