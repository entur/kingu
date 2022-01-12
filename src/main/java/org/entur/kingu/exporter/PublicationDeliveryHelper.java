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

package org.entur.kingu.exporter;

import org.entur.kingu.model.FareFrame;
import org.entur.kingu.model.LocaleStructure;
import org.entur.kingu.model.MultilingualStringEntity;
import org.entur.kingu.model.ServiceFrame;
import org.entur.kingu.model.VersionFrameDefaultsStructure;
import org.entur.kingu.netex.id.NetexIdHelper;
import org.entur.kingu.time.ExportTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PublicationDeliveryHelper {

    private final NetexIdHelper netexIdHelper;

    private final ExportTimeZone exportTimeZone;

    private final String defaultExportLanguage;

    public PublicationDeliveryHelper(NetexIdHelper netexIdHelper,
                                     ExportTimeZone exportTimeZone,
                                     @Value("${tiamat.locals.language.default:nor}") String defaultExportLanguage) {
        this.netexIdHelper = netexIdHelper;
        this.exportTimeZone = exportTimeZone;
        this.defaultExportLanguage = defaultExportLanguage;
    }

    public org.entur.kingu.model.SiteFrame createTiamatSiteFrame(String description) {
        org.entur.kingu.model.SiteFrame siteFrame = new org.entur.kingu.model.SiteFrame();
        siteFrame.setFrameDefaults(getFrameDefaultLocale());
        siteFrame.setDescription(new MultilingualStringEntity(description));
        siteFrame.setVersion(1L);
        siteFrame.setNetexId(netexIdHelper.getNetexId(siteFrame, siteFrame.hashCode()));
        return siteFrame;
    }

    public FareFrame createTiamatFareFrame(String description) {
        FareFrame fareFrame = new FareFrame();
        fareFrame.setDescription(new MultilingualStringEntity(description));
        fareFrame.setFrameDefaults(getFrameDefaultLocale());
        fareFrame.setVersion(1L);
        fareFrame.setNetexId(netexIdHelper.getNetexId(fareFrame, fareFrame.hashCode()));

        return fareFrame;
    }

    public ServiceFrame createTiamatServiceFrame(String description) {
        ServiceFrame serviceFrame= new ServiceFrame();
        serviceFrame.setDescription(new MultilingualStringEntity(description));
        serviceFrame.setFrameDefaults(getFrameDefaultLocale());
        serviceFrame.setVersion(1L);
        serviceFrame.setNetexId(netexIdHelper.getNetexId(serviceFrame, serviceFrame.hashCode()));

        return serviceFrame;
    }

    private VersionFrameDefaultsStructure getFrameDefaultLocale() {

        LocaleStructure localeStructure = new LocaleStructure();
        localeStructure.setTimeZone(exportTimeZone.getDefaultTimeZoneId().toString());
        localeStructure.setDefaultLanguage(defaultExportLanguage);
        VersionFrameDefaultsStructure versionFrameDefaultsStructure = new VersionFrameDefaultsStructure();
        versionFrameDefaultsStructure.setDefaultLocale(localeStructure);
        return versionFrameDefaultsStructure;
    }

}
