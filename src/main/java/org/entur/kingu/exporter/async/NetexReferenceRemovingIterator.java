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

package org.entur.kingu.exporter.async;

import jakarta.xml.bind.JAXBElement;
import org.entur.kingu.config.ExportMode;
import org.entur.kingu.config.ExportParams;
import org.rutebanken.netex.model.StopPlace;
import org.rutebanken.netex.model.ValidBetween;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class NetexReferenceRemovingIterator implements Iterator<StopPlace> {

    public final Iterator<StopPlace> iterator;

    Map<String,String> currentStops;

    private final Consumer<StopPlace> tariffZoneVersionRemover;

    private final Consumer<StopPlace> topographicPlaceVersionRemover;

    private final Consumer<StopPlace> fareZoneVersionRemover;

    private final Consumer<StopPlace> stopPlaceValidityRemover;

    public NetexReferenceRemovingIterator(Iterator<StopPlace> iterator, ExportParams exportParams,  Map<String,String> currentStops) {
        this.iterator = iterator;
        this.currentStops = currentStops;
        this.stopPlaceValidityRemover = this::removeStoPlaceValidity;
        Consumer<StopPlace> doNothingConsumer = s -> {};

        if (exportParams.getTariffZoneExportMode().equals(ExportMode.NONE)) {
            tariffZoneVersionRemover = stopPlace -> removeTariffZoneRefsVersion(stopPlace,"TariffZone");
        } else {
            tariffZoneVersionRemover = doNothingConsumer;
        }
        if(exportParams.getFareZoneExportMode().equals(ExportMode.NONE)) {
            fareZoneVersionRemover = stopPlace -> removeTariffZoneRefsVersion(stopPlace,"FareZone");
        } else {
            fareZoneVersionRemover = doNothingConsumer;
        }


        if (exportParams.getTopographicPlaceExportMode().equals(ExportMode.NONE)) {
            topographicPlaceVersionRemover = this::removeTopographicPlaceRef;
        } else {
            topographicPlaceVersionRemover = doNothingConsumer;
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public StopPlace next() {
        StopPlace next = iterator.next();
        if(next != null) {
            tariffZoneVersionRemover.accept(next);
            fareZoneVersionRemover.accept(next);
            topographicPlaceVersionRemover.accept(next);
            stopPlaceValidityRemover.accept(next);
        }
        return next;
    }

    /*
     *  This method makes sure that current  stop place is valid ,if a stop place has been changed/updated
     *  while export is in progress.
    */

    private void removeStoPlaceValidity(StopPlace stopPlace) {

        final List<ValidBetween> validBetweenList = stopPlace.getValidBetween();

        if (validBetweenList != null && currentStops.containsKey(stopPlace.getId()) && stopPlace.getVersion().equals(currentStops.get(stopPlace.getId()))) {
            validBetweenList.forEach(validBetween -> validBetween.setToDate(null));
        }

    }

    private void removeTariffZoneRefsVersion(StopPlace stopPlace, String type) {
        if(stopPlace.getTariffZones() != null && stopPlace.getTariffZones().getTariffZoneRef_() != null) {
            stopPlace.getTariffZones().getTariffZoneRef_().stream()
                    .map(JAXBElement::getValue)
                    .forEach(tariffZoneRef -> {
                if(tariffZoneRef.getRef().contains(type)) {
                    tariffZoneRef.setVersion(null);
                }
            });
        }
    }

    private void removeTopographicPlaceRef(StopPlace stopPlace) {
        if(stopPlace.getTopographicPlaceRef() != null) {
            stopPlace.getTopographicPlaceRef().setVersion(null);
        }
    }
}
