package org.entur.kingu.service;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.config.TiamatExportConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;



//TODO should be removed /move to client kakka

@Service
public class TaskGenerator {

    public static final Logger logger= LoggerFactory.getLogger(TaskGenerator.class);

    private final TiamatExportConfig tiamatExportConfig;

    private final String inComingNetexExport;

    public TaskGenerator(TiamatExportConfig tiamatExportConfig,
                         @Value("${kingu.incoming.camel.route.topic.netex.export}") String inComingNetexExport) {
        this.tiamatExportConfig = tiamatExportConfig;
        this.inComingNetexExport = inComingNetexExport;
    }


    public void addExportTasks(Exchange exchange) throws IOException {
        logger.info("Add Export Tasks");
        final List<ExportParams> exportJobs = tiamatExportConfig.getExportJobs();

        try (ProducerTemplate template = exchange.getContext().createProducerTemplate()) {
            for (ExportParams exportJob : exportJobs) {
                    template.sendBody(inComingNetexExport, exportJob.toString());

            }
        }

    }

}
