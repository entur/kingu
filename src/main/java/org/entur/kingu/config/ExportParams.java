package org.entur.kingu.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.entur.kingu.repository.search.StopPlaceSearch;
import org.entur.kingu.service.NetexExportJobBuilderException;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

public class ExportParams implements Serializable {

    private String name;
    private ExportMode tariffZoneExportMode = ExportMode.RELEVANT;
    private ExportMode fareZoneExportMode = ExportMode.RELEVANT;
    private ExportMode groupOfStopPlacesExportMode = ExportMode.RELEVANT;
    private ExportMode groupOfTariffZonesExportMode = ExportMode.RELEVANT;
    private ExportMode topographicPlaceExportMode = ExportMode.RELEVANT;
    private List<String> municipalityReferences;
    private List<String> countyReferences;
    private List<String> countryReferences;
    private StopPlaceSearch stopPlaceSearch;
    private boolean exportMultiSurface = false;


    public ExportParams() {
    }

    public ExportParams(String name,
                        ExportMode tariffZoneExportMode,
                        ExportMode fareZoneExportMode,
                        ExportMode groupOfStopPlacesExportMode,
                        ExportMode groupOfTariffZonesExportMode,
                        ExportMode topographicPlaceExportMode,
                        List<String> municipalityReferences,
                        List<String> countyReferences,
                        List<String> countryReferences,
                        StopPlaceSearch stopPlaceSearch) {
        this.name = name;
        this.tariffZoneExportMode = tariffZoneExportMode;
        this.fareZoneExportMode = fareZoneExportMode;
        this.groupOfStopPlacesExportMode = groupOfStopPlacesExportMode;
        this.groupOfTariffZonesExportMode = groupOfTariffZonesExportMode;
        this.topographicPlaceExportMode = topographicPlaceExportMode;
        this.municipalityReferences = municipalityReferences;
        this.countyReferences = countyReferences;
        this.countryReferences = countryReferences;
        this.stopPlaceSearch = stopPlaceSearch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExportMode getTariffZoneExportMode() {
        return tariffZoneExportMode;
    }

    public ExportMode getFareZoneExportMode() {
        return fareZoneExportMode;
    }

    public ExportMode getGroupOfStopPlacesExportMode() {
        return groupOfStopPlacesExportMode;
    }

    public ExportMode getGroupOfTariffZonesExportMode() {
        return groupOfTariffZonesExportMode;
    }

    public ExportMode getTopographicPlaceExportMode() {
        return topographicPlaceExportMode;
    }

    public List<String> getMunicipalityReferences() {
        return municipalityReferences;
    }

    public List<String> getCountyReferences() {
        return countyReferences;
    }

    public List<String> getCountryReferences() {
        return countryReferences;
    }

    public StopPlaceSearch getStopPlaceSearch() {
        return stopPlaceSearch;
    }

    public boolean isExportMultiSurface() {
        return exportMultiSurface;
    }

    public void setExportMultiSurface(boolean exportMultiSurface) {
        this.exportMultiSurface = exportMultiSurface;
    }

    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ExportParams fromString(String exportJob) throws NetexExportJobBuilderException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(exportJob, ExportParams.class);
        } catch (JsonProcessingException e) {
            throw new NetexExportJobBuilderException(e.getMessage());
        }
    }

}
