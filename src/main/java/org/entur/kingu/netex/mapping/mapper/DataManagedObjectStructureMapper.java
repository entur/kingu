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

package org.entur.kingu.netex.mapping.mapper;

import com.google.common.collect.ImmutableMap;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.DataManagedObjectStructure;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;


@Component
public class DataManagedObjectStructureMapper extends CustomMapper<DataManagedObjectStructure, org.entur.kingu.model.DataManagedObjectStructure> {

    public static final String CHANGED_BY = "CHANGED_BY";
    public static final String VERSION_COMMENT = "VERSION_COMMENT";

    private final NetexIdMapper netexIdMapper;


    /**
     * Properties to map to key values in netex format. Getters for the tiamat entity.
     */
    private static final Map<String, Function<org.entur.kingu.model.DataManagedObjectStructure, String>> tiamatEntityGetFunctions = new ImmutableMap.Builder<String, Function<org.entur.kingu.model.DataManagedObjectStructure, String>>()
            // Since netex export are  open data therefore it's not desirable to publish username.
            //.put(CHANGED_BY, org.entur.kingu.model.DataManagedObjectStructure::getChangedBy)
            .put(VERSION_COMMENT, org.entur.kingu.model.DataManagedObjectStructure::getVersionComment)
            .build();

    @Autowired
    public DataManagedObjectStructureMapper(NetexIdMapper netexIdMapper) {
        this.netexIdMapper = netexIdMapper;
    }

    @Override
    public void mapAtoB(DataManagedObjectStructure netexEntity, org.entur.kingu.model.DataManagedObjectStructure tiamatEntity, MappingContext context) {
        // not implemented
    }

    @Override
    public void mapBtoA(org.entur.kingu.model.DataManagedObjectStructure tiamatEntity, DataManagedObjectStructure netexEntity, MappingContext context) {
        netexIdMapper.toNetexModel(tiamatEntity, netexEntity);
        netexEntity.setVersion(String.valueOf(tiamatEntity.getVersion()));

        if (netexEntity.getKeyList() == null) {
            netexEntity.withKeyList(new KeyListStructure());
        }
        tiamatEntityGetFunctions.forEach((property, function) -> setKey(netexEntity, property, function.apply(tiamatEntity)));
        if (netexEntity.getKeyList().getKeyValue() == null || netexEntity.getKeyList().getKeyValue().isEmpty()) {
            // Do not allow empty key list
            netexEntity.withKeyList(null);
        }
    }

    private void setKey(DataManagedObjectStructure netexEntity, String key, String value) {
        if (value == null) return;

        netexEntity.getKeyList()
                .withKeyValue(new KeyValueStructure()
                        .withKey(key)
                        .withValue(value));
    }
}

