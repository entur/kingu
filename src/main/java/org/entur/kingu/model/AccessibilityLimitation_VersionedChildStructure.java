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

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;


@MappedSuperclass
public class AccessibilityLimitation_VersionedChildStructure
        extends org.entur.kingu.model.VersionedChildStructure {

    @Enumerated(EnumType.STRING)
    protected org.entur.kingu.model.LimitationStatusEnumeration wheelchairAccess;

    @Enumerated(EnumType.STRING)
    protected org.entur.kingu.model.LimitationStatusEnumeration stepFreeAccess;

    @Enumerated(EnumType.STRING)
    protected org.entur.kingu.model.LimitationStatusEnumeration escalatorFreeAccess;

    @Enumerated(EnumType.STRING)
    protected org.entur.kingu.model.LimitationStatusEnumeration liftFreeAccess;

    @Enumerated(EnumType.STRING)
    protected org.entur.kingu.model.LimitationStatusEnumeration audibleSignalsAvailable;

    @Enumerated(EnumType.STRING)
    protected org.entur.kingu.model.LimitationStatusEnumeration visualSignsAvailable;

    public org.entur.kingu.model.LimitationStatusEnumeration getWheelchairAccess() {
        return wheelchairAccess;
    }

    public void setWheelchairAccess(org.entur.kingu.model.LimitationStatusEnumeration value) {
        this.wheelchairAccess = value;
    }

    public org.entur.kingu.model.LimitationStatusEnumeration getStepFreeAccess() {
        return stepFreeAccess;
    }

    public void setStepFreeAccess(org.entur.kingu.model.LimitationStatusEnumeration value) {
        this.stepFreeAccess = value;
    }

    public org.entur.kingu.model.LimitationStatusEnumeration getEscalatorFreeAccess() {
        return escalatorFreeAccess;
    }

    public void setEscalatorFreeAccess(org.entur.kingu.model.LimitationStatusEnumeration value) {
        this.escalatorFreeAccess = value;
    }

    public org.entur.kingu.model.LimitationStatusEnumeration getLiftFreeAccess() {
        return liftFreeAccess;
    }

    public void setLiftFreeAccess(org.entur.kingu.model.LimitationStatusEnumeration value) {
        this.liftFreeAccess = value;
    }

    public org.entur.kingu.model.LimitationStatusEnumeration getAudibleSignalsAvailable() {
        return audibleSignalsAvailable;
    }

    public void setAudibleSignalsAvailable(org.entur.kingu.model.LimitationStatusEnumeration value) {
        this.audibleSignalsAvailable = value;
    }

    public org.entur.kingu.model.LimitationStatusEnumeration getVisualSignsAvailable() {
        return visualSignsAvailable;
    }

    public void setVisualSignsAvailable(org.entur.kingu.model.LimitationStatusEnumeration value) {
        this.visualSignsAvailable = value;
    }

}
