package org.entur.kingu.netex.mapping.mapper;

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.entur.kingu.model.TariffZoneRef;
import org.rutebanken.netex.model.GroupOfTariffZones;

import java.util.List;

public class GroupOfTariffZonesMapper extends CustomMapper<GroupOfTariffZones, org.entur.kingu.model.GroupOfTariffZones> {

    @Override
    public void mapAtoB(GroupOfTariffZones netexGroupOfTariffZones, org.entur.kingu.model.GroupOfTariffZones tiamatGroupOfTariffZones, MappingContext context) {
        super.mapAtoB(netexGroupOfTariffZones, tiamatGroupOfTariffZones, context);
        if (netexGroupOfTariffZones.getMembers() != null && !netexGroupOfTariffZones.getMembers().getTariffZoneRef_().isEmpty()) {
            final List<TariffZoneRef> tiamatTariffZoneRefList = netexGroupOfTariffZones.getMembers().getTariffZoneRef_().stream()
                    .map(JAXBElement::getValue)
                    .map(tzr -> new TariffZoneRef(tzr.getRef())).toList();

            tiamatGroupOfTariffZones.getMembers().addAll(tiamatTariffZoneRefList);
        }
    }

    @Override
    public void mapBtoA(org.entur.kingu.model.GroupOfTariffZones tiamatGroupOfTariffZones, GroupOfTariffZones netexGroupOfTariffZones, MappingContext context) {
        super.mapBtoA(tiamatGroupOfTariffZones, netexGroupOfTariffZones, context);
    }
}
