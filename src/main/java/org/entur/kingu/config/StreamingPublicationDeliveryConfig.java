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

package org.entur.kingu.config;


import org.entur.kingu.exporter.PublicationDeliveryHelper;
import org.entur.kingu.exporter.StreamingPublicationDelivery;
import org.entur.kingu.netex.id.NetexIdHelper;
import org.entur.kingu.netex.id.ValidPrefixList;
import org.entur.kingu.netex.mapping.NetexMapper;
import org.entur.kingu.repository.FareZoneRepository;
import org.entur.kingu.repository.GroupOfStopPlacesRepository;
import org.entur.kingu.repository.GroupOfTariffZonesRepository;
import org.entur.kingu.repository.ParkingRepository;
import org.entur.kingu.repository.StopPlaceRepository;
import org.entur.kingu.repository.TariffZoneRepository;
import org.entur.kingu.repository.TopographicPlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

import java.io.IOException;

@Configuration
public class StreamingPublicationDeliveryConfig {

    @Autowired
    private StopPlaceRepository stopPlaceRepository;

    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private PublicationDeliveryHelper publicationDeliveryHelper;

    @Autowired
    private NetexMapper netexMapper;

    @Autowired
    private ValidPrefixList validPrefixList;

    @Autowired
    private TariffZoneRepository tariffZoneRepository;

    @Autowired
    private FareZoneRepository fareZoneRepository;

    @Autowired
    private TopographicPlaceRepository topographicPlaceRepository;

    @Autowired
    private GroupOfStopPlacesRepository groupOfStopPlacesRepository;

    @Autowired
    private GroupOfTariffZonesRepository groupOfTariffZonesRepository;

    @Autowired
    private NetexIdHelper netexIdHelper;

    @Value("${asyncNetexExport.validateAgainstSchema:false}")
    private boolean validateAsyncExport;

    @Value("${syncNetexExport.validateAgainstSchema:true}")
    private boolean validateSyncExport;

    @Bean("asyncStreamingPublicationDelivery")
    public StreamingPublicationDelivery asyncStreamingPublicationDelivery() throws IOException, SAXException {
        return createStreamingPublicationDelivery(validateAsyncExport);
    }

    @Bean("syncStreamingPublicationDelivery")
    public StreamingPublicationDelivery syncStreamingPublicationDelivery() throws IOException, SAXException {
        return createStreamingPublicationDelivery(validateSyncExport);
    }

    private StreamingPublicationDelivery createStreamingPublicationDelivery(boolean validate) throws IOException, SAXException {
        return new StreamingPublicationDelivery(stopPlaceRepository, parkingRepository, validPrefixList,
                publicationDeliveryHelper, netexMapper, tariffZoneRepository, fareZoneRepository, topographicPlaceRepository,
                groupOfStopPlacesRepository,groupOfTariffZonesRepository, netexIdHelper, validate);
    }
}
