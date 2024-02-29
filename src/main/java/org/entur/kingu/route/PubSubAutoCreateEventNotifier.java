package org.entur.kingu.route;

import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.google.pubsub.GooglePubsubEndpoint;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.DefaultInterceptSendToEndpoint;
import org.apache.camel.support.EventNotifierSupport;
import org.entur.pubsub.base.EnturGooglePubSubAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Create PubSub topics and subscriptions on startup.
 * This is used only in unit tests and local environment.
 */
@Component
@Profile("google-pubsub-autocreate")
public class PubSubAutoCreateEventNotifier extends EventNotifierSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubSubAutoCreateEventNotifier.class);
    private final EnturGooglePubSubAdmin enturGooglePubSubAdmin;

    @Autowired
    private PubSubAdmin pubSubAdmin;

    private final String netexExportTopic;

    private final String netexExportSubscription;

    public PubSubAutoCreateEventNotifier(EnturGooglePubSubAdmin enturGooglePubSubAdmin,
                                                     @Value("${pubsub.kingu.outbound.topic.netex.export}") String netexExportTopic,
                                                     @Value("${pubsub.kingu.inbound.subscription.netex.export}") String netexExportSubscription) {
        this.enturGooglePubSubAdmin = enturGooglePubSubAdmin;
        this.netexExportTopic = getTopicSubscriptionName(netexExportTopic);
        this.netexExportSubscription = getTopicSubscriptionName(netexExportSubscription);
    }

    @Override
    public void notify(CamelEvent event) {

        if (event instanceof CamelEvent.CamelContextStartingEvent camelContextStartingEvent) {
            CamelContext context = camelContextStartingEvent.getContext();
            context.getEndpoints().stream().filter(e -> e.getEndpointUri().contains("google-pubsub:")).forEach(this::createSubscriptionIfMissing);
        }

    }

    private void createSubscriptionIfMissing(Endpoint e) {
        GooglePubsubEndpoint gep;
        if (e instanceof GooglePubsubEndpoint googlePubsubEndpoint) {
            gep = googlePubsubEndpoint;
        } else if (e instanceof DefaultInterceptSendToEndpoint defaultInterceptSendToEndpoint) {
            gep = (GooglePubsubEndpoint) defaultInterceptSendToEndpoint.getOriginalEndpoint();
        } else {
            throw new IllegalStateException("Incompatible endpoint: " + e);
        }
        final String destination = gep.getDestinationName();
        if (destination.equals(netexExportTopic) || destination.equals(netexExportSubscription) ) {
            createSubscriptionTopic(netexExportTopic,netexExportSubscription);
        } else {
            enturGooglePubSubAdmin.createSubscriptionIfMissing(destination);
        }
    }

    /*
     * enturGooglePubSubAdmin.createSubscriptionIfMissing only support to create topic and subscription with same name
     * with this implementation we can set custom topics and subscriptions with different names
     */
    private void createSubscriptionTopic(String topicName,String subscriptionName) {

        try {
            pubSubAdmin.createTopic(topicName);
            LOGGER.debug("Created topic: {}", topicName);
        } catch (AlreadyExistsException e) {
            LOGGER.trace("Did not create topic: {}, as it already exists", topicName);
        }

        try {
            pubSubAdmin.createSubscription(subscriptionName, topicName);
            LOGGER.debug("Created subscription: {} with topic: {}", subscriptionName, topicName);
        } catch (AlreadyExistsException e) {
            LOGGER.trace("Did not create subscription: {}, as it already exists", subscriptionName);
        }
    }

    private String getTopicSubscriptionName(String endpoint) {

        final String[] endpointParts = endpoint.split(":");

        if (endpointParts.length == 3) {
            return endpointParts[2];
        } else {
            throw new IllegalArgumentException(
                    "Google PubSub Endpoint format \"google-pubsub:projectId:destinationName[:subscriptionName]\"");
        }
    }

}
