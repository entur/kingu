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


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.NotImplementedException;
import org.entur.kingu.model.TopographicPlace;
import org.entur.kingu.repository.iterator.ScrollableResultIterator;
import org.entur.kingu.repository.search.TopographicPlaceQueryFromSearchBuilder;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Transactional
public class TopographicPlaceRepositoryImpl implements TopographicPlaceRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;


	@Autowired
	private TopographicPlaceQueryFromSearchBuilder topographicPlaceQueryFromSearchBuilder;


	@Override
	public String findFirstByKeyValues(String key, Set<String> originalIds) {
		throw new NotImplementedException("findByKeyValue not implemented for topographic place");
	}

	@Override
	public Iterator<TopographicPlace> scrollTopographicPlaces(Set<Long> stopPlaceDbIds) {

		if(stopPlaceDbIds == null || stopPlaceDbIds.isEmpty()) {
			return Collections.emptyIterator();
		}

		return scrollTopographicPlaces(generateTopographicPlacesQueryFromStopPlaceIds(stopPlaceDbIds));
	}

	@Override
	public Iterator<TopographicPlace> scrollTopographicPlaces() {
		return scrollTopographicPlaces("SELECT t.* FROM topographic_place t");
	}

	public Iterator<TopographicPlace> scrollTopographicPlaces(String sql) {
		Session session = entityManager.unwrap(Session.class);
		NativeQuery sqlQuery = session.createNativeQuery(sql);

		sqlQuery.addEntity(TopographicPlace.class);
		sqlQuery.setReadOnly(true);
		sqlQuery.setFetchSize(1000);
		sqlQuery.setCacheable(false);
		ScrollableResults results = sqlQuery.scroll(ScrollMode.FORWARD_ONLY);
		ScrollableResultIterator<TopographicPlace> topographicPlaceIterator = new ScrollableResultIterator<>(results, 100, session);
		return  topographicPlaceIterator;
	}

	private String generateTopographicPlacesQueryFromStopPlaceIds(Set<Long> stopPlaceDbIds) {

		Set<String> stopPlaceStringDbIds = stopPlaceDbIds.stream().map(lvalue -> String.valueOf(lvalue)).collect(Collectors.toSet());
		String joinedStopPlaceDbIds = String.join(",", stopPlaceStringDbIds);
		StringBuilder sql = new StringBuilder("SELECT tp.* " +
				"FROM ( " +
				"  SELECT tp1.id " +
				"  FROM topographic_place tp1 " +
				"  INNER JOIN stop_place sp " +
				"    ON sp.topographic_place_id = tp1.id " +
				"  WHERE sp.id IN(");
		sql.append(joinedStopPlaceDbIds);
		sql.append(") " +
				"  GROUP BY tp1.id " +
				") tp1 " +
				"JOIN topographic_place tp ON tp.id = tp1.id");
		return sql.toString();
	}
}
