package org.entur.kingu.service;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.exporter.AsyncPublicationDeliveryExporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ExportJobInitiator {
    public static final Logger logger = LoggerFactory.getLogger(ExportJobInitiator.class);
    @Autowired
    AsyncPublicationDeliveryExporter asyncPublicationDeliveryExporter;

    public void startExport(Exchange exchange, @Header(value = Exchange.BREADCRUMB_ID) String breadcrumbId) {
        final String body = exchange.getIn().getBody(String.class);
        final ExportParams exportParams = ExportParams.fromString(body);

        logger.info("Task name: {}, and queryPram {}", exportParams.getName(), exportParams.getStopPlaceSearch().getVersionValidity());

        asyncPublicationDeliveryExporter.startExportJob(exportParams,breadcrumbId);

        exchange.getIn().setBody(exportParams.toString());

    }
}
