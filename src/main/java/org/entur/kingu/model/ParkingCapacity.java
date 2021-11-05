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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigInteger;

@Entity
public class ParkingCapacity
        extends org.entur.kingu.model.VersionedChildStructure {

    protected org.entur.kingu.model.SiteElementRefStructure parentRef;
    @Enumerated(EnumType.STRING)
    protected ParkingUserEnumeration parkingUserType;
    @Enumerated(EnumType.STRING)
    protected ParkingVehicleEnumeration parkingVehicleType;
    @Enumerated(EnumType.STRING)
    protected org.entur.kingu.model.ParkingStayEnumeration parkingStayType;
    protected BigInteger numberOfSpaces;
    protected BigInteger numberOfSpacesWithRechargePoint;

    public org.entur.kingu.model.SiteElementRefStructure getParentRef() {
        return parentRef;
    }

    public void setParentRef(org.entur.kingu.model.SiteElementRefStructure value) {
        this.parentRef = value;
    }

    public ParkingUserEnumeration getParkingUserType() {
        return parkingUserType;
    }

    public void setParkingUserType(ParkingUserEnumeration value) {
        this.parkingUserType = value;
    }

    public ParkingVehicleEnumeration getParkingVehicleType() {
        return parkingVehicleType;
    }

    public void setParkingVehicleType(ParkingVehicleEnumeration value) {
        this.parkingVehicleType = value;
    }

    public org.entur.kingu.model.ParkingStayEnumeration getParkingStayType() {
        return parkingStayType;
    }

    public void setParkingStayType(org.entur.kingu.model.ParkingStayEnumeration value) {
        this.parkingStayType = value;
    }

    public BigInteger getNumberOfSpaces() {
        return numberOfSpaces;
    }

    public void setNumberOfSpaces(BigInteger value) {
        this.numberOfSpaces = value;
    }

    public BigInteger getNumberOfSpacesWithRechargePoint() {
        return numberOfSpacesWithRechargePoint;
    }

    public void setNumberOfSpacesWithRechargePoint(BigInteger numberOfSpacesWithRechargePoint) {
        this.numberOfSpacesWithRechargePoint = numberOfSpacesWithRechargePoint;
    }


}
