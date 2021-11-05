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

public class OpenTransportMode_ValueStructure
        extends org.entur.kingu.model.TypeOfValue_VersionStructure {

    protected AllModesEnumeration transportMode;
    protected AirSubmodeEnumeration airSubmode;
    protected org.entur.kingu.model.BusSubmodeEnumeration busSubmode;
    protected org.entur.kingu.model.CoachSubmodeEnumeration coachSubmode;
    protected FunicularSubmodeEnumeration funicularSubmode;
    protected MetroSubmodeEnumeration metroSubmode;
    protected TramSubmodeEnumeration tramSubmode;
    protected TelecabinSubmodeEnumeration telecabinSubmode;
    protected org.entur.kingu.model.RailSubmodeEnumeration railSubmode;
    protected org.entur.kingu.model.WaterSubmodeEnumeration waterSubmode;
    protected TaxiSubmodeEnumeration taxiSubmode;
    protected org.entur.kingu.model.SelfDriveSubmodeEnumeration selfDriveSubmode;
    protected SubmodeRefStructure submodeRef;

    public AllModesEnumeration getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(AllModesEnumeration value) {
        this.transportMode = value;
    }

    public AirSubmodeEnumeration getAirSubmode() {
        return airSubmode;
    }

    public void setAirSubmode(AirSubmodeEnumeration value) {
        this.airSubmode = value;
    }

    public org.entur.kingu.model.BusSubmodeEnumeration getBusSubmode() {
        return busSubmode;
    }

    public void setBusSubmode(org.entur.kingu.model.BusSubmodeEnumeration value) {
        this.busSubmode = value;
    }

    public org.entur.kingu.model.CoachSubmodeEnumeration getCoachSubmode() {
        return coachSubmode;
    }

    public void setCoachSubmode(org.entur.kingu.model.CoachSubmodeEnumeration value) {
        this.coachSubmode = value;
    }

    public FunicularSubmodeEnumeration getFunicularSubmode() {
        return funicularSubmode;
    }

    public void setFunicularSubmode(FunicularSubmodeEnumeration value) {
        this.funicularSubmode = value;
    }

    public MetroSubmodeEnumeration getMetroSubmode() {
        return metroSubmode;
    }

    public void setMetroSubmode(MetroSubmodeEnumeration value) {
        this.metroSubmode = value;
    }

    public TramSubmodeEnumeration getTramSubmode() {
        return tramSubmode;
    }

    public void setTramSubmode(TramSubmodeEnumeration value) {
        this.tramSubmode = value;
    }

    public TelecabinSubmodeEnumeration getTelecabinSubmode() {
        return telecabinSubmode;
    }

    public void setTelecabinSubmode(TelecabinSubmodeEnumeration value) {
        this.telecabinSubmode = value;
    }

    public org.entur.kingu.model.RailSubmodeEnumeration getRailSubmode() {
        return railSubmode;
    }

    public void setRailSubmode(org.entur.kingu.model.RailSubmodeEnumeration value) {
        this.railSubmode = value;
    }

    public org.entur.kingu.model.WaterSubmodeEnumeration getWaterSubmode() {
        return waterSubmode;
    }

    public void setWaterSubmode(org.entur.kingu.model.WaterSubmodeEnumeration value) {
        this.waterSubmode = value;
    }

    public TaxiSubmodeEnumeration getTaxiSubmode() {
        return taxiSubmode;
    }

    public void setTaxiSubmode(TaxiSubmodeEnumeration value) {
        this.taxiSubmode = value;
    }

    public org.entur.kingu.model.SelfDriveSubmodeEnumeration getSelfDriveSubmode() {
        return selfDriveSubmode;
    }

    public void setSelfDriveSubmode(org.entur.kingu.model.SelfDriveSubmodeEnumeration value) {
        this.selfDriveSubmode = value;
    }

    public SubmodeRefStructure getSubmodeRef() {
        return submodeRef;
    }

    public void setSubmodeRef(SubmodeRefStructure value) {
        this.submodeRef = value;
    }

}
