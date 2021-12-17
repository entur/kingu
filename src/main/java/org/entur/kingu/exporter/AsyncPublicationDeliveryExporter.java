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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.camel.CamelContext;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.exporter.async.ExportJobWorker;

import org.entur.kingu.netex.validation.NetexXmlReferenceValidator;
import org.entur.kingu.service.BlobStoreService;
import org.entur.kingu.time.ExportTimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class AsyncPublicationDeliveryExporter {

    private static final Logger logger = LoggerFactory.getLogger(AsyncPublicationDeliveryExporter.class);

    private static final ExecutorService exportService = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
            .setNameFormat("exporter-%d").build());


    private final StreamingPublicationDelivery streamingPublicationDelivery;

    private final NetexXmlReferenceValidator netexXmlReferenceValidator;

    private final ExportTimeZone exportTimeZone;

    private final String localExportPath;

    private final CamelContext camelContext;

    private final String outGoingNetexExport;

    private final BlobStoreService blobStoreService;


    @Autowired
    public AsyncPublicationDeliveryExporter(@Qualifier("asyncStreamingPublicationDelivery") StreamingPublicationDelivery streamingPublicationDelivery,
                                            NetexXmlReferenceValidator netexXmlReferenceValidator,
                                            ExportTimeZone exportTimeZone,
                                            @Value("${async.export.path:/deployments/data/}") String localExportPath,
                                            CamelContext camelContext,
                                            @Value("${kingu.outgoing.camel.route.topic.netex.export}") String outGoingNetexExport,
                                            BlobStoreService blobStoreService) {
        this.streamingPublicationDelivery = streamingPublicationDelivery;
        this.netexXmlReferenceValidator = netexXmlReferenceValidator;
        this.exportTimeZone = exportTimeZone;
        this.localExportPath = localExportPath;
        this.camelContext = camelContext;
        this.outGoingNetexExport = outGoingNetexExport;
        this.blobStoreService = blobStoreService;


        File exportFolder = new File(localExportPath);
        if(!exportFolder.exists() && !exportFolder.mkdirs()) {
            throw new RuntimeException("Cannot find or create export directory from path: " + localExportPath +
                    ". Please create the directory with correct permissions, or configure a different path with the property async.export.path");
        }
        if(!exportFolder.canWrite()) {
            throw new RuntimeException("Cannot write to path: " + localExportPath +
                    ". Please create the directory with correct permissions, or configure a different path with the property async.export.path");
        }
        logger.info("Verified local export path {}", localExportPath);
    }

    /**
     * Start export job with upload to google cloud storage
     * @param exportParams search params for stops
     */
    public void startExportJob(ExportParams exportParams,String breadcrumbId) {
        ExportJobWorker exportJobWorker = new ExportJobWorker(blobStoreService,
                exportParams,
                exportTimeZone,
                streamingPublicationDelivery,
                localExportPath,
                netexXmlReferenceValidator,
                camelContext,
                outGoingNetexExport,
                breadcrumbId);
        logger.info("Sending export job to exportService: {}", exportParams);
        exportService.submit(exportJobWorker);
    }

}
