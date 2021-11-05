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

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.math.BigInteger;

@MappedSuperclass
public class CycleStorageEquipment_VersionStructure
        extends org.entur.kingu.model.PlaceEquipment_VersionStructure {

    protected BigInteger numberOfSpaces;
    protected org.entur.kingu.model.CycleStorageEnumeration cycleStorageType;
    @Transient
    protected Boolean cage;
    @Transient
    protected Boolean covered;

    public BigInteger getNumberOfSpaces() {
        return numberOfSpaces;
    }

    public void setNumberOfSpaces(BigInteger value) {
        this.numberOfSpaces = value;
    }

    public org.entur.kingu.model.CycleStorageEnumeration getCycleStorageType() {
        return cycleStorageType;
    }

    public void setCycleStorageType(org.entur.kingu.model.CycleStorageEnumeration value) {
        this.cycleStorageType = value;
    }

    public Boolean isCage() {
        return cage;
    }

    public void setCage(Boolean value) {
        this.cage = value;
    }

    public Boolean isCovered() {
        return covered;
    }

    public void setCovered(Boolean value) {
        this.covered = value;
    }

}
