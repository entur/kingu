package org.entur.kingu.route;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.google.pubsub.GooglePubsubConstants;
import org.apache.camel.model.RouteDefinition;
import org.entur.kingu.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.entur.kingu.Constants.SINGLETON_ROUTE_DEFINITION_GROUP_NAME;

/**
 * Defines common route behavior.
 */
@Component
public abstract class BaseRouteBuilder extends RouteBuilder {

    @Value("${kingu.camel.redelivery.max:1}")
    private int maxRedelivery;

    @Value("${kingu.camel.redelivery.delay:5000}")
    private int redeliveryDelay;

    @Value("${kingu.camel.redelivery.backoff.multiplier:3}")
    private int backOffMultiplier;


    @Override
    public void configure() throws Exception {
        errorHandler(defaultErrorHandler()
                .redeliveryDelay(redeliveryDelay)
                .maximumRedeliveries(maxRedelivery)
                .onRedelivery(this::logRedelivery)
                .useExponentialBackOff()
                .backOffMultiplier(backOffMultiplier)
                .logExhausted(true)
                .logRetryStackTrace(true));

        // Copy all PubSub headers except the internal Camel PubSub headers from the PubSub message into the Camel message headers.
        interceptFrom(".*google-pubsub:.*")
                .process(exchange ->
                {
                    Map<String, String> pubSubAttributes = exchange.getIn().getHeader(GooglePubsubConstants.ATTRIBUTES, Map.class);
                    if (pubSubAttributes == null) {
                        throw new IllegalStateException("Missing PubSub attribute maps in Exchange");
                    }
                    pubSubAttributes.entrySet()
                            .stream()
                            .filter(entry -> !entry.getKey().startsWith("CamelGooglePubsub"))
                            .forEach(entry -> exchange.getIn().setHeader(entry.getKey(), entry.getValue()));
                });

        // Copy all PubSub headers except the internal Camel PubSub headers from the Camel message into the PubSub message.
        interceptSendToEndpoint("google-pubsub:*").process(
                exchange -> {
                    Map<String, String> pubSubAttributes = new HashMap<>();
                    exchange.getIn().getHeaders().entrySet().stream()
                            .filter(entry -> !entry.getKey().startsWith("CamelGooglePubsub"))
                            .filter(entry -> Objects.toString(entry.getValue()).length() <= 1024)
                            .forEach(entry -> pubSubAttributes.put(entry.getKey(), Objects.toString(entry.getValue(), "")));
                    exchange.getIn().setHeader(GooglePubsubConstants.ATTRIBUTES, pubSubAttributes);

                });

    }

    protected void logRedelivery(Exchange exchange) {
        int redeliveryCounter = exchange.getIn().getHeader("CamelRedeliveryCounter", Integer.class);
        int redeliveryMaxCounter = exchange.getIn().getHeader("CamelRedeliveryMaxCounter", Integer.class);
        Throwable camelCaughtThrowable = exchange.getProperty("CamelExceptionCaught", Throwable.class);

        log.warn("Exchange failed, redelivering the message locally, attempt {}/{}...",redeliveryCounter, redeliveryMaxCounter, camelCaughtThrowable);
    }

    protected String logDebugShowAll() {
        return "log:" + getClass().getName() + "?level=DEBUG&showAll=true&multiline=true";
    }


    protected void setNewCorrelationId(Exchange e) {
        e.getIn().setHeader(Constants.CORRELATION_ID, UUID.randomUUID().toString());
    }

    protected void setCorrelationIdIfMissing(Exchange e) {
        e.getIn().setHeader(Constants.CORRELATION_ID, e.getIn().getHeader(Constants.CORRELATION_ID, UUID.randomUUID().toString()));
    }


    /**
     * Create a new singleton route definition from URI. Only one such route should be active throughout the cluster at any time.
     */
    protected RouteDefinition singletonFrom(String uri) {
        return this.from(uri).group(SINGLETON_ROUTE_DEFINITION_GROUP_NAME);
    }



}
