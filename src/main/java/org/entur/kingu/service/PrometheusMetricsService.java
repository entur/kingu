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

package org.entur.kingu.service;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrometheusMetricsService {

    private static final String METRICS_PREFIX = "app.kingu.";
    private static final String NETEX_EXPORT_COUNTER_NAME = METRICS_PREFIX + "netex.export.count";
    private static final String NETEX_EXPORT_TIMER_NAME = METRICS_PREFIX + "netex.export.timer";

    private final PrometheusMeterRegistry registry;

    private PrometheusMetricsService(@Autowired PrometheusMeterRegistry registry) {
        this.registry = registry;
    }

    @PreDestroy
    public void shutdown() {
        registry.close();
    }

    public void exportCounter() {
        List<Tag> counterTags = new ArrayList<>();
        counterTags.add(new ImmutableTag("netexExport", "kingu"));

        registry.counter(NETEX_EXPORT_COUNTER_NAME, counterTags).increment();
    }


    public Timer exportTimer(String exportName) {
        List<Tag> counterTags = new ArrayList<>();
        counterTags.add(new ImmutableTag(NETEX_EXPORT_TIMER_NAME, exportName));

        return registry.timer(NETEX_EXPORT_TIMER_NAME, counterTags);
    }

}
