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

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.math.BigInteger;


public class PointOnLink_VersionedChildStructure
        extends org.entur.kingu.model.VersionedChildStructure {

    protected org.entur.kingu.model.MultilingualStringEntity name;
    protected org.entur.kingu.model.LinkRefStructure linkRef;
    protected BigDecimal distanceFromStart;
    protected JAXBElement<? extends org.entur.kingu.model.PointRefStructure> pointRef;
    protected JAXBElement<? extends org.entur.kingu.model.Point> point;
    protected BigInteger order;

    public org.entur.kingu.model.MultilingualStringEntity getName() {
        return name;
    }

    public void setName(org.entur.kingu.model.MultilingualStringEntity value) {
        this.name = value;
    }

    public org.entur.kingu.model.LinkRefStructure getLinkRef() {
        return linkRef;
    }

    public void setLinkRef(org.entur.kingu.model.LinkRefStructure value) {
        this.linkRef = value;
    }

    public BigDecimal getDistanceFromStart() {
        return distanceFromStart;
    }

    public void setDistanceFromStart(BigDecimal value) {
        this.distanceFromStart = value;
    }

    public JAXBElement<? extends org.entur.kingu.model.PointRefStructure> getPointRef() {
        return pointRef;
    }

    public void setPointRef(JAXBElement<? extends org.entur.kingu.model.PointRefStructure> value) {
        this.pointRef = value;
    }

    public JAXBElement<? extends org.entur.kingu.model.Point> getPoint() {
        return point;
    }

    public void setPoint(JAXBElement<? extends org.entur.kingu.model.Point> value) {
        this.point = value;
    }

    public BigInteger getOrder() {
        return order;
    }

    public void setOrder(BigInteger value) {
        this.order = value;
    }

}
