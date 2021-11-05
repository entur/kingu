package org.entur.kingu.repository.search;



import org.entur.kingu.config.ExportParams;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;



@Service
public class ExportParamsAndStopPlaceSearchValidator {

    public void validateExportParams(ExportParams exportParams) {

        StopPlaceSearch stopPlaceSearch = exportParams.getStopPlaceSearch();
        Set<String> paramsExplicitySet = new HashSet<>();

        addIfTrue("allVersions", stopPlaceSearch.isAllVersions(), paramsExplicitySet);
        addIfNonNull("version", stopPlaceSearch.getVersion(), paramsExplicitySet);
        addIfNonNull("pointInTime", stopPlaceSearch.getPointInTime(), paramsExplicitySet);
        addIfNonNull("versionValidity", stopPlaceSearch.getVersionValidity(), paramsExplicitySet);

        if (paramsExplicitySet.size() > 1) {
            String message = "Parameters cannot be combined: " + paramsExplicitySet + ". Remove one of them";
            throw new IllegalArgumentException(message);
        }
    }

    private void addIfNonNull(String name, Object value, Set<String> paramsExplicitySet) {
        if (value != null) {
            paramsExplicitySet.add(name);
        }
    }

    private void addIfTrue(String name, boolean value, Set<String> set) {
        if (value) {
            set.add(name);
        }
    }
}
