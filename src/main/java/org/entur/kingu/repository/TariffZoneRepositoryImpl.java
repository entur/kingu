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


import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.entur.kingu.config.ExportParams;
import org.entur.kingu.config.VersionValidity;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.hibernate.query.NativeQuery;


import org.entur.kingu.model.TariffZone;
import org.entur.kingu.repository.iterator.ScrollableResultIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public class TariffZoneRepositoryImpl implements TariffZoneRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(TariffZoneRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;



    @Override
    public Optional<TariffZone> findValidTariffZone(String netexId) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder sql = new StringBuilder("SELECT tz.* FROM tariff_zone tz WHERE " +
                "tz.version = (SELECT MAX(tzv.version) FROM tariff_zone tzv WHERE tzv.netex_id = tz.netex_id " +
                "and (tzv.to_date is null or tzv.to_date > :pointInTime) and (tzv.from_date is null or tzv.from_date < :pointInTime))");
        Instant pointInTime = Instant.now();
        parameters.put("pointInTime", pointInTime);

        sql.append("AND tz.netex_id =:netexId");
        parameters.put("netexId", netexId);


        Query query = entityManager.createNativeQuery(sql.toString(), TariffZone.class);
        parameters.forEach(query::setParameter);

        return query.getResultList().stream().findFirst();
    }

    @Override
    public List<TariffZone> findAllValidTariffZones() {
        Map<String, Object> parameters = new HashMap<>();
        String sql = "SELECT tz.* FROM tariff_zone tz WHERE " +
                "tz.version = (SELECT MAX(tzv.version) FROM tariff_zone tzv WHERE tzv.netex_id = tz.netex_id " +
                "and (tzv.to_date is null or tzv.to_date > :pointInTime) and (tzv.from_date is null or tzv.from_date < :pointInTime))";
        Instant pointInTime = Instant.now();
        parameters.put("pointInTime", pointInTime);

        Query query = entityManager.createNativeQuery(sql, TariffZone.class);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

    @Override
    public List<TariffZone> getTariffZonesFromStopPlaceIds(Set<Long> stopPlaceIds) {
        if (stopPlaceIds == null || stopPlaceIds.isEmpty()) {
            return new ArrayList<>();
        }

        Query query = entityManager.createNativeQuery(generateTariffZoneQueryFromStopPlaceIds(stopPlaceIds), TariffZone.class);

        @SuppressWarnings("unchecked")
        List<TariffZone> tariffZones = query.getResultList();
        return tariffZones;
    }

    @Override
    public List<TariffZone> findTariffZones(ExportParams exportParams) {
        var sql = new StringBuilder("select tz.* from tariff_zone tz");

        if (exportParams.getStopPlaceSearch() != null && exportParams.getStopPlaceSearch().getVersionValidity() !=null) {
            if (exportParams.getStopPlaceSearch().getVersionValidity().equals(VersionValidity.CURRENT)) {
                logger.info("Preparing to scroll only current tariff zones");
                sql.append(" WHERE tz.version = (SELECT MAX(tzv.version) FROM tariff_zone tzv WHERE tzv.netex_id = tz.netex_id " +
                        "and (tzv.to_date is null or tzv.to_date > now()) and (tzv.from_date is null or tzv.from_date < now()))");
            } else if (exportParams.getStopPlaceSearch().getVersionValidity().equals(VersionValidity.CURRENT_FUTURE)) {
                logger.info("Preparing to scroll current and future tariff zones");
                sql.append(" WHERE (tz.to_date is null or tz.to_date > now())");
            }
        }

        Session session = entityManager.unwrap(Session.class);
        NativeQuery sqlQuery = session.createNativeQuery(sql.toString());

        sqlQuery.addEntity(TariffZone.class);
        sqlQuery.setReadOnly(true);
        sqlQuery.setCacheable(false);
        return sqlQuery.getResultList();
    }

    @Override
    public Iterator<TariffZone> scrollTariffZones(Set<Long> stopPlaceDbIds) {

        if (stopPlaceDbIds == null || stopPlaceDbIds.isEmpty()) {
            return new ArrayList<TariffZone>().iterator();
        }
        return scrollTariffZones(generateTariffZoneQueryFromStopPlaceIds(stopPlaceDbIds));
    }

    public Iterator<TariffZone> scrollTariffZones(String sql) {
        Session session = entityManager.unwrap(Session.class);
        NativeQuery sqlQuery = session.createNativeQuery(sql);

        sqlQuery.addEntity(TariffZone.class);
        sqlQuery.setReadOnly(true);
        sqlQuery.setFetchSize(100);
        sqlQuery.setCacheable(false);
        ScrollableResults results = sqlQuery.scroll(ScrollMode.FORWARD_ONLY);
        return new ScrollableResultIterator<>(results, 100, session);
    }


    private String generateTariffZoneQueryFromStopPlaceIds(Set<Long> stopPlaceDbIds) {
        var sql = "select" +
                "        tz.* " +
                "    from" +
                "        (         select" +
                "            tz1.id         " +
                "        from" +
                "            stop_place_tariff_zones sptz     " +
                "        inner join" +
                "            tariff_zone tz1              " +
                "                ON tz1.netex_id = sptz.ref         " +
                "                AND  sptz.stop_place_id IN(" + StringUtils.join(stopPlaceDbIds,',') +
                "                )            " +
                "                AND (" +
                "                    (" +
                "                        sptz.version IS NOT NULL                          " +
                "                        AND cast(tz1.version AS text) = sptz.version                           " +
                "                    )                                   " +
                "                    OR (" +
                "                        sptz.version IS NULL                          " +
                "                        AND tz1.version = (" +
                "                            SELECT" +
                "                                MAX(tz2.version)                          " +
                "                        FROM" +
                "                            tariff_zone tz2                          " +
                "                        WHERE" +
                "                            tz2.netex_id = tz1.netex_id                                                         " +
                "                            AND tz2.from_date < NOW()                                   " +
                "                    )                       " +
                "                )                 " +
                "            )           " +
                "        GROUP BY" +
                "            tz1.id      ) tz1      " +
                "        join" +
                "            tariff_zone tz      " +
                "                on tz.id = tz1.id";


        logger.debug(sql);
        return sql;
    }

    @Override
    public String findFirstByKeyValues(String key, Set<String> originalIds) {
        return null;
    }
}
