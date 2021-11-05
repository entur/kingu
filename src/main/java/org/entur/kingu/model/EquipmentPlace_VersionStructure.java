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

package org.entur.kingu.model;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.List;


@MappedSuperclass
public class EquipmentPlace_VersionStructure extends org.entur.kingu.model.Place {
    @OneToMany(cascade = CascadeType.ALL)
    protected List<org.entur.kingu.model.EquipmentPosition> equipmentPositions;

    @Transient
    protected org.entur.kingu.model.Equipments_RelStructure placeEquipments;

    public List<org.entur.kingu.model.EquipmentPosition> getEquipmentPositions() {
        return equipmentPositions;
    }

    public void setEquipmentPositions(List<org.entur.kingu.model.EquipmentPosition> equipmentPositions) {
        this.equipmentPositions = equipmentPositions;
    }

    public org.entur.kingu.model.Equipments_RelStructure getPlaceEquipments() {
        return placeEquipments;
    }

    public void setPlaceEquipments(org.entur.kingu.model.Equipments_RelStructure value) {
        this.placeEquipments = value;
    }

}
