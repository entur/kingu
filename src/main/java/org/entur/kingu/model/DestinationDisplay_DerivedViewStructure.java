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

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;


@MappedSuperclass
public class DestinationDisplay_DerivedViewStructure
        extends org.entur.kingu.model.DerivedViewStructure {


    @Transient
    protected org.entur.kingu.model.DestinationDisplayRefStructure destinationDisplayRef;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected org.entur.kingu.model.MultilingualStringEntity name;

    @Transient
    protected org.entur.kingu.model.MultilingualStringEntity shortName;

    @Transient
    protected org.entur.kingu.model.MultilingualStringEntity sideText;

    @Transient
    protected org.entur.kingu.model.MultilingualStringEntity frontText;

    @Transient
    protected org.entur.kingu.model.MultilingualStringEntity driverDisplayText;

    protected String shortCode;

    protected String publicCode;

    @Transient
    protected org.entur.kingu.model.PrivateCodeStructure privateCode;

    public org.entur.kingu.model.DestinationDisplayRefStructure getDestinationDisplayRef() {
        return destinationDisplayRef;
    }

    public void setDestinationDisplayRef(org.entur.kingu.model.DestinationDisplayRefStructure value) {
        this.destinationDisplayRef = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getName() {
        return name;
    }

    public void setName(org.entur.kingu.model.MultilingualStringEntity value) {
        this.name = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getShortName() {
        return shortName;
    }

    public void setShortName(org.entur.kingu.model.MultilingualStringEntity value) {
        this.shortName = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getSideText() {
        return sideText;
    }

    public void setSideText(org.entur.kingu.model.MultilingualStringEntity value) {
        this.sideText = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getFrontText() {
        return frontText;
    }

    public void setFrontText(org.entur.kingu.model.MultilingualStringEntity value) {
        this.frontText = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getDriverDisplayText() {
        return driverDisplayText;
    }

    public void setDriverDisplayText(org.entur.kingu.model.MultilingualStringEntity value) {
        this.driverDisplayText = value;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String value) {
        this.shortCode = value;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(String value) {
        this.publicCode = value;
    }

    public org.entur.kingu.model.PrivateCodeStructure getPrivateCode() {
        return privateCode;
    }

    public void setPrivateCode(org.entur.kingu.model.PrivateCodeStructure value) {
        this.privateCode = value;
    }

}
