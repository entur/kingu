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

package org.entur.kingu.repository;

import org.apache.commons.lang3.StringUtils;
import org.entur.kingu.model.GroupOfStopPlaces;
import org.entur.kingu.repository.iterator.ScrollableResultIterator;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GroupOfStopPlacesRepositoryImpl implements org.entur.kingu.repository.GroupOfStopPlacesRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(GroupOfStopPlaces.class);

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<GroupOfStopPlaces> getGroupOfStopPlacesFromStopPlaceIds(Set<Long> stopPlaceIds) {
        if (stopPlaceIds == null || stopPlaceIds.isEmpty()) {
            return new ArrayList<>();
        }

        Query query = entityManager.createNativeQuery(generateGroupOfStopPlacesQueryFromStopPlaceIds(stopPlaceIds), GroupOfStopPlaces.class);

        @SuppressWarnings("unchecked")
        List<GroupOfStopPlaces> groupOfStopPlaces = query.getResultList();
        return groupOfStopPlaces;
    }

    @Override
    public Iterator<GroupOfStopPlaces> scrollGroupOfStopPlaces() {

        return scrollGroupOfStopPlaces("select gosp.* from group_of_stop_places gosp where " +
                "gosp.version = (select max(gospv.version) from group_of_stop_places gospv where gospv.netex_id = gosp.netex_id)");
    }

    @Override
    public Iterator<GroupOfStopPlaces> scrollGroupOfStopPlaces(Set<Long> stopPlaceDbIds) {

        if (stopPlaceDbIds == null || stopPlaceDbIds.isEmpty()) {
            return Collections.emptyIterator();
        }
        return scrollGroupOfStopPlaces(generateGroupOfStopPlacesQueryFromStopPlaceIds(stopPlaceDbIds));
    }

    private Iterator<GroupOfStopPlaces> scrollGroupOfStopPlaces(String sql) {
        Session session = entityManager.unwrap(Session.class);
        NativeQuery sqlQuery = session.createNativeQuery(sql);

        final int fetchSize = 100;

        sqlQuery.addEntity(GroupOfStopPlaces.class);
        sqlQuery.setReadOnly(true);
        sqlQuery.setFetchSize(fetchSize);
        sqlQuery.setCacheable(false);
        ScrollableResults results = sqlQuery.scroll(ScrollMode.FORWARD_ONLY);
        return new ScrollableResultIterator<>(results, fetchSize, session);
    }
    private String generateGroupOfStopPlacesQueryFromStopPlaceIds(Set<Long> stopPlaceDbIds) {
        StringBuilder sqlStringBuilder = new StringBuilder("SELECT g.* FROM " +
                "   (SELECT gosp1.id, gosp1.netex_id " +
                "       FROM stop_place s " +
                "       INNER JOIN group_of_stop_places_members members ON members.ref = s.netex_id " +
                "       INNER JOIN group_of_stop_places gosp1 ON members.group_of_stop_places_id = gosp1.id " +
                "       WHERE s.id IN (");

        sqlStringBuilder.append(StringUtils.join(stopPlaceDbIds, ','));

        sqlStringBuilder.append("   ) " +
                "      AND gosp1.version = " +
                "       (SELECT max(gospv.version) " +
                "           FROM group_of_stop_places gospv " +
                "           WHERE gospv.netex_id = gosp1.netex_id) " +
                "      GROUP BY gosp1.id, gosp1.netex_id " +
                "  ) gosp " +
                "JOIN group_of_stop_places g ON gosp.netex_id = g.netex_id");

        String sql = sqlStringBuilder.toString();
        logger.debug(sql);
        return sql;
    }
}

