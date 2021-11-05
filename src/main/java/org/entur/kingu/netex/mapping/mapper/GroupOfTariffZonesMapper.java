package org.entur.kingu.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.GroupOfTariffZones;
import org.entur.kingu.model.TariffZoneRef;

import java.util.List;
import java.util.stream.Collectors;

public class GroupOfTariffZonesMapper extends CustomMapper<GroupOfTariffZones, org.entur.kingu.model.GroupOfTariffZones> {

    @Override
    public void mapAtoB(GroupOfTariffZones netexGroupOfTariffZones, org.entur.kingu.model.GroupOfTariffZones tiamatGroupOfTariffZones, MappingContext context) {
        super.mapAtoB(netexGroupOfTariffZones, tiamatGroupOfTariffZones, context);
        if (netexGroupOfTariffZones.getMembers() != null && !netexGroupOfTariffZones.getMembers().getTariffZoneRef().isEmpty()) {
            final List<TariffZoneRef> tiamatTariffZoneRefList = netexGroupOfTariffZones.getMembers().getTariffZoneRef().stream()
                    .map(tzr -> new TariffZoneRef(tzr.getRef())).collect(Collectors.toList());

            tiamatGroupOfTariffZones.getMembers().addAll(tiamatTariffZoneRefList);
        }
    }

    @Override
    public void mapBtoA(org.entur.kingu.model.GroupOfTariffZones tiamatGroupOfTariffZones, GroupOfTariffZones netexGroupOfTariffZones, MappingContext context) {
        super.mapBtoA(tiamatGroupOfTariffZones, netexGroupOfTariffZones, context);
    }
}
