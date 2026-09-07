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

package org.entur.kingu.exporter.async;

import com.google.common.base.Strings;
import org.entur.kingu.model.EmbeddableMultilingualString;
import org.entur.kingu.model.SiteRefStructure;
import org.entur.kingu.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Appends parent stop places to the iteration the first time a child referencing them is encountered.
 * Parents are looked up from a preloaded map (built once per export via a single bulk query) rather than
 * fetched one-by-one, since the same set of parents is otherwise walked repeatedly (once per stop place
 * frame being built) and a per-parent query would multiply DB round trips for every large export.
 */
public class ParentStopFetchingIterator implements Iterator<StopPlace> {

    private static final Logger logger = LoggerFactory.getLogger(ParentStopFetchingIterator.class);

    private final Iterator<StopPlace> iterator;

    private final Map<String, StopPlace> preloadedParentsByRef;


    private final Map<String, EmbeddableMultilingualString> parents = new HashMap<>();

    private StopPlace parent = null;

    public ParentStopFetchingIterator(Iterator<StopPlace> iterator, Map<String, StopPlace> preloadedParentsByRef) {
        this.iterator = iterator;
        this.preloadedParentsByRef = preloadedParentsByRef != null ? preloadedParentsByRef : Collections.emptyMap();
    }

    @Override
    public boolean hasNext() {
        return parent != null || iterator.hasNext();
    }

    @Override
    public StopPlace next() {

        if (parent != null) {
            StopPlace next = parent;
            parent = null;
            return next;
        }


        StopPlace stopPlace = iterator.next();
        if (stopPlace.getParentSiteRef() != null) {
            String parentRefString = refString(stopPlace.getParentSiteRef());
            if (!parents.containsKey(parentRefString)) {
                StopPlace preloadedParent = preloadedParentsByRef.get(parentRefString);
                if (preloadedParent == null) {
                    logger.warn("No preloaded parent found for ref: {}. Parent will not be appended to export.", parentRefString);
                } else {
                    parent = preloadedParent;
                    logger.debug("Emitting preloaded parent during iteration: {} - {}", parent.getNetexId(), parent.getVersion());
                }
                parents.put(parentRefString, parent == null ? null : parent.getName());
            }
            copyNameFromParentIfMissing(parentRefString, parents.get(parentRefString), stopPlace);

        }

        return stopPlace;
    }

    public boolean hasNextParent() {
        return parent != null;
    }

    public void copyNameFromParentIfMissing(String parentRefString, EmbeddableMultilingualString parentName, StopPlace childStopPlace) {
        if (childStopPlace.getName() == null || Strings.isNullOrEmpty(childStopPlace.getName().getValue())) {
            logger.debug("Copying name: {} from parent {} to child stop: {}", parentName, parentRefString, childStopPlace.getNetexId());
            childStopPlace.setName(parentName);
        }
    }


    private String refString(SiteRefStructure siteRefStructure) {
        return siteRefStructure.getRef() + "-" + siteRefStructure.getVersion();
    }
}
