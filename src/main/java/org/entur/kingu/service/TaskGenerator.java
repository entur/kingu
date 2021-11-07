package org.entur.kingu.service;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.config.TiamatExportConfig;
import org.entur.kingu.route.export.TiamatExportTaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.entur.kingu.Constants.TASK_TYPE;

//TODO should be removed /move to client kakka

@Service
public class TaskGenerator {

    public static final Logger logger= LoggerFactory.getLogger(TaskGenerator.class);

    private final TiamatExportConfig tiamatExportConfig;

    public TaskGenerator(TiamatExportConfig tiamatExportConfig) {
        this.tiamatExportConfig = tiamatExportConfig;
    }


    public void addExportTasks(Exchange exchange) throws IOException {
        logger.info("Add Export Tasks");
        final List<ExportParams> exportJobs = tiamatExportConfig.getExportJobs();

        try (ProducerTemplate template = exchange.getContext().createProducerTemplate()) {
            for (ExportParams exportJob : exportJobs) {
                    template.sendBodyAndHeader("activemq:TiamatExportQueue", exportJob.toString(),TASK_TYPE, TiamatExportTaskType.PROCESS.toString());

            }
        }

    }

}
