package org.entur.kingu.route.export;

import org.apache.camel.LoggingLevel;
import org.apache.camel.processor.ThrottlerRejectedExecutionException;
import org.entur.kingu.route.BaseRouteBuilder;
import org.entur.kingu.service.ExportJobInitiator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NetexExportRoute extends BaseRouteBuilder {

    @Value("${kingu.incoming.camel.route.topic.netex.export}")
    private String inComingNetexExport;

    @Value("${kingu.outgoing.camel.route.topic.netex.export}")
    private String outGoingNetexExport;

    @Override
    public void configure() throws Exception {
        super.configure();

        onException(ThrottlerRejectedExecutionException.class).continued(true)
                .log("There are more than one inComing messages");

        from(inComingNetexExport)
                .throttle(1).rejectExecution(true)
                .log(LoggingLevel.INFO, "Starting Tiamat exports-1: ${body}")
                .to("direct:tiamatExport")
                .routeId("from-tiamat-export-queue-process");

        from("direct:tiamatExport")
                .log(LoggingLevel.INFO,"Start tiamat export: ${body}")
                .bean(ExportJobInitiator.class,"startExport")
                .routeId("tiamat-export");
    }


}
