package org.entur.kingu.repository;

import org.entur.kingu.model.PurposeOfGrouping;
import org.entur.kingu.repository.iterator.ScrollableResultIterator;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Iterator;
import java.util.List;

public class PurposeOfGroupingRepositoryImpl implements PurposeOfGroupingRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PurposeOfGrouping> findAllPurposeOfGrouping() {
        String sql = "SELECT pog.* FROM purpose_of_grouping pog";
        Query query = entityManager.createNativeQuery(sql, PurposeOfGrouping.class);
        return query.getResultList();
    }

    @Override
    public Iterator<PurposeOfGrouping> scrollPurposeOfGrouping(){
        final int fetchSize = 100;
        Session session = entityManager.unwrap(Session.class);
        String sql = "SELECT pog.* FROM purpose_of_grouping pog";
        NativeQuery query = session.createNativeQuery(sql);
        query.addEntity(PurposeOfGrouping.class);
        query.setReadOnly(true);
        query.setFetchSize(fetchSize);
        query.setCacheable(false);
        final ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);

        final ScrollableResultIterator<PurposeOfGrouping> pogEnityIterator = new ScrollableResultIterator<>(results, fetchSize, session);

        return  pogEnityIterator;
    }
}
