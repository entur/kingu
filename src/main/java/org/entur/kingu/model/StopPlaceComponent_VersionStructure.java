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

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;


@MappedSuperclass
public abstract class StopPlaceComponent_VersionStructure
        extends org.entur.kingu.model.SiteComponent_VersionStructure {

    @Transient
    protected VehicleModeEnumeration transportMode;

    @Transient
    protected AirSubmodeEnumeration airSubmode;

    @Transient
    protected org.entur.kingu.model.BusSubmodeEnumeration busSubmode;

    @Transient
    protected org.entur.kingu.model.CoachSubmodeEnumeration coachSubmode;

    @Transient
    protected org.entur.kingu.model.FunicularSubmodeEnumeration funicularSubmode;

    @Transient
    protected org.entur.kingu.model.MetroSubmodeEnumeration metroSubmode;

    @Transient
    protected org.entur.kingu.model.TramSubmodeEnumeration tramSubmode;

    @Transient
    protected org.entur.kingu.model.TelecabinSubmodeEnumeration telecabinSubmode;

    @Transient
    protected org.entur.kingu.model.RailSubmodeEnumeration railSubmode;

    @Transient
    protected org.entur.kingu.model.WaterSubmodeEnumeration waterSubmode;

    @Transient
    protected List<VehicleModeEnumeration> otherTransportModes;

    public StopPlaceComponent_VersionStructure(org.entur.kingu.model.EmbeddableMultilingualString name) {
        super(name);
    }

    public StopPlaceComponent_VersionStructure() {
    }

    public VehicleModeEnumeration getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(VehicleModeEnumeration value) {
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

    public org.entur.kingu.model.FunicularSubmodeEnumeration getFunicularSubmode() {
        return funicularSubmode;
    }

    public void setFunicularSubmode(org.entur.kingu.model.FunicularSubmodeEnumeration value) {
        this.funicularSubmode = value;
    }

    public org.entur.kingu.model.MetroSubmodeEnumeration getMetroSubmode() {
        return metroSubmode;
    }

    public void setMetroSubmode(org.entur.kingu.model.MetroSubmodeEnumeration value) {
        this.metroSubmode = value;
    }

    public org.entur.kingu.model.TramSubmodeEnumeration getTramSubmode() {
        return tramSubmode;
    }

    public void setTramSubmode(org.entur.kingu.model.TramSubmodeEnumeration value) {
        this.tramSubmode = value;
    }

    public org.entur.kingu.model.TelecabinSubmodeEnumeration getTelecabinSubmode() {
        return telecabinSubmode;
    }

    public void setTelecabinSubmode(org.entur.kingu.model.TelecabinSubmodeEnumeration value) {
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

    public List<VehicleModeEnumeration> getOtherTransportModes() {
        if (otherTransportModes == null) {
            otherTransportModes = new ArrayList<>();
        }
        return this.otherTransportModes;
    }

}
