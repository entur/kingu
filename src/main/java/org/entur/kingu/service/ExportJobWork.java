package org.entur.kingu.service;

import org.apache.camel.Exchange;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.route.export.TiamatExportTaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static org.entur.kingu.Constants.TASK_TYPE;

@Service
public class ExportJobWork {
    public static final Logger logger= LoggerFactory.getLogger(ExportJobWork.class);
    public void startExport(Exchange exchange) {
        final String body = exchange.getIn().getBody(String.class);
        final ExportParams exportJob = ExportParams.fromString(body);
        //final TiamatExportTask tiamatExportTask = TiamatExportTask.fromString(body);
        logger.info("Task name: {}, and queryPram {}",exportJob.getName(),exportJob.getVersionValidity());

        //tiamatExportTask.setUrl("gs://tiamat-dev/export/1234.xml");
        exchange.getIn().removeHeader(TASK_TYPE);
        exchange.getIn().setBody(exportJob.toString());
        exchange.getIn().setHeader(TASK_TYPE, TiamatExportTaskType.PROCESSED.toString());
    }
}
