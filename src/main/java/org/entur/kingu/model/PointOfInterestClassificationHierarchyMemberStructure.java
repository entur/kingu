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

public class PointOfInterestClassificationHierarchyMemberStructure
        extends org.entur.kingu.model.VersionedChildStructure {

    protected org.entur.kingu.model.PointOfInterestHierarchyRefStructure pointOfInterestHierarchyRef;
    protected org.entur.kingu.model.PointOfInterestClassificationRefStructure parentClassificationRef;
    protected org.entur.kingu.model.PointOfInterestClassificationRefStructure pointOfInterestClassificationRef;

    public org.entur.kingu.model.PointOfInterestHierarchyRefStructure getPointOfInterestHierarchyRef() {
        return pointOfInterestHierarchyRef;
    }

    public void setPointOfInterestHierarchyRef(org.entur.kingu.model.PointOfInterestHierarchyRefStructure value) {
        this.pointOfInterestHierarchyRef = value;
    }

    public org.entur.kingu.model.PointOfInterestClassificationRefStructure getParentClassificationRef() {
        return parentClassificationRef;
    }

    public void setParentClassificationRef(org.entur.kingu.model.PointOfInterestClassificationRefStructure value) {
        this.parentClassificationRef = value;
    }

    public org.entur.kingu.model.PointOfInterestClassificationRefStructure getPointOfInterestClassificationRef() {
        return pointOfInterestClassificationRef;
    }

    public void setPointOfInterestClassificationRef(org.entur.kingu.model.PointOfInterestClassificationRefStructure value) {
        this.pointOfInterestClassificationRef = value;
    }

}
