package org.entur.kingu.repository;



import org.entur.kingu.model.PurposeOfGrouping;

import java.util.Iterator;
import java.util.List;

public interface PurposeOfGroupingRepositoryCustom {
    List<PurposeOfGrouping> findAllPurposeOfGrouping();

    Iterator<PurposeOfGrouping> scrollPurposeOfGrouping();
}
