package org.entur.kingu.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.GroupOfStopPlaces;

public class GroupOfStopPlacesMapper extends CustomMapper<GroupOfStopPlaces, org.entur.kingu.model.GroupOfStopPlaces> {

    @Override
    public void mapAtoB(GroupOfStopPlaces netexGroupOfStopPlaces, org.entur.kingu.model.GroupOfStopPlaces tiamatGroupOfStopPlaces, MappingContext context) {
        super.mapAtoB(netexGroupOfStopPlaces, tiamatGroupOfStopPlaces, context);

    }

    @Override
    public void mapBtoA(org.entur.kingu.model.GroupOfStopPlaces tiamatGroupOfStopPlaces, GroupOfStopPlaces netexGroupOfStopPlaces, MappingContext context) {
        super.mapBtoA(tiamatGroupOfStopPlaces, netexGroupOfStopPlaces, context);
    }
}
