package org.entur.kingu.route.rest;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.entur.kingu.service.TaskGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

//TODO should be moved to cleint
@Component
public class RestRoute extends RouteBuilder {

    private static final String PLAIN = "text/plain";

    @Value("#{'${tiamat.publish.export:}'.split(';')}")
    private List<String> exportConfigStrings;

    @Autowired
    TaskGenerator taskGenerator;

    @Override
    public void configure() throws Exception {


        restConfiguration()
                .component("jetty").port(8090);

        rest("/export")
                .post("/stop_places")
                .description("Trigger export from Stop Place Registry (NSR) for all existing configurations")
                .consumes(PLAIN)
                .produces(PLAIN)
                .responseMessage().code(200).message("Command accepted").endResponseMessage()
                .route()
                .removeHeaders("CamelHttp*")
                .removeHeaders("Authorization")
                .log(LoggingLevel.INFO, "Starting Tiamat exports from rest")
                .process(e -> {
                    taskGenerator.addExportTasks(e);
                })
                .setBody(simple("done"))
                .routeId("admin-tiamat-publish-export-full")
                .endRest();
    }
}
