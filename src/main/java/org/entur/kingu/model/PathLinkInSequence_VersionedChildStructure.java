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

public class PathLinkInSequence_VersionedChildStructure
        extends org.entur.kingu.model.LinkInLinkSequence_VersionedChildStructure {

    protected org.entur.kingu.model.PathLinkRefStructure pathLinkRef;
    protected org.entur.kingu.model.MultilingualStringEntity description;
    protected Boolean reverse;
    protected PathHeadingEnumeration heading;
    protected TransitionEnumeration transition;
    protected org.entur.kingu.model.MultilingualStringEntity label;

    public org.entur.kingu.model.PathLinkRefStructure getPathLinkRef() {
        return pathLinkRef;
    }

    public void setPathLinkRef(org.entur.kingu.model.PathLinkRefStructure value) {
        this.pathLinkRef = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getDescription() {
        return description;
    }

    public void setDescription(org.entur.kingu.model.MultilingualStringEntity value) {
        this.description = value;
    }

    public Boolean isReverse() {
        return reverse;
    }

    public void setReverse(Boolean value) {
        this.reverse = value;
    }

    public PathHeadingEnumeration getHeading() {
        return heading;
    }

    public void setHeading(PathHeadingEnumeration value) {
        this.heading = value;
    }

    public TransitionEnumeration getTransition() {
        return transition;
    }

    public void setTransition(TransitionEnumeration value) {
        this.transition = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getLabel() {
        return label;
    }

    public void setLabel(org.entur.kingu.model.MultilingualStringEntity value) {
        this.label = value;
    }

}
