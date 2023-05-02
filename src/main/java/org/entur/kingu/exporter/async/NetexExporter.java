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


import org.apache.camel.Exchange;
import org.entur.kingu.exporter.StreamingPublicationDelivery;
import org.entur.kingu.model.job.ExportJob;
import org.entur.kingu.model.job.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;


@Service
public class NetexExporter {

    private static final Logger logger = LoggerFactory.getLogger(NetexExporter.class);

    private final StreamingPublicationDelivery streamingPublicationDelivery;


    public NetexExporter(StreamingPublicationDelivery streamingPublicationDelivery) {
        this.streamingPublicationDelivery = streamingPublicationDelivery;
    }

    public ExportJob process(Exchange exchange) throws InterruptedException, IOException, XMLStreamException, SAXException, JAXBException {

        // TODO: camelContext.setUseMDCLogging(true);    camelContext.setUseBreadcrumb(true);
        MDC.put("camel.breadcrumbId", (String) exchange.getIn().getHeader(Exchange.BREADCRUMB_ID));
        final ExportJob exportJob = exchange.getIn().getBody(ExportJob.class);
        try {


            logger.info("Export export job: {}", exportJob);

            File localExportXmlFile = exportJob.getLocalExportXmlFile();
            localExportXmlFile.createNewFile();
            logger.info("Start streaming publication delivery to local file {}", localExportXmlFile);
            FileOutputStream fileOutputStream = new FileOutputStream(localExportXmlFile,false);
            streamingPublicationDelivery.stream(exportJob.getExportParams(), fileOutputStream, true);

            exportJob.setStatus(JobStatus.FINISHED);
            exportJob.setFinished(Instant.now());
        } finally {
            MDC.remove("camel.breadcrumbId");
        }

        return exportJob;

    }

}
