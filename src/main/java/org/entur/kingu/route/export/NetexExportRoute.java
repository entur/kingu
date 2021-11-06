package org.entur.kingu.route.export;

import org.apache.camel.LoggingLevel;
import org.entur.kingu.route.BaseRouteBuilder;
import org.entur.kingu.service.ExportJobWork;
import org.springframework.stereotype.Component;

@Component
public class NetexExportRoute extends BaseRouteBuilder {
    @Override
    public void configure() throws Exception {
        super.configure();

        from("activemq:TiamatExportQueue?selector=taskType='PROCESS'")
                .log(LoggingLevel.INFO, "Starting Tiamat exports-1: ${body}")
                .to("direct:tiamatExport")
                .routeId("from-tiamat-export-queue-process");

        from("direct:tiamatExport")
                .log(LoggingLevel.INFO,"Start tiamat export: ${body}")
                .bean(ExportJobWork.class,"startExport")
                .routeId("tiamat-export");


        from("activemq:TiamatExportQueue?selector=taskType='PROCESSED'")
                .log(LoggingLevel.INFO, "Done processing Tiamat exports: ${body}")
                .log(LoggingLevel.INFO,"Export location is $simple{in.header.exportLocation}")
                .routeId("from-tiamat-export-queue-processed");
    }
}
