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

import com.google.common.base.MoreObjects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class Equipment_VersionStructure
        extends EntityInVersionStructure {

    @Transient
    protected org.entur.kingu.model.MultilingualStringEntity name;

    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "private_code_value")),
            @AttributeOverride(name = "type", column = @Column(name = "private_code_type"))
    })
    @Embedded
    protected org.entur.kingu.model.PrivateCodeStructure privateCode;
    @Transient
    protected org.entur.kingu.model.PrivateCodeStructure publicCode;
    @Transient
    protected String image;
    @Transient
    protected TypeOfEquipmentRefStructure typeOfEquipmentRef;
    @Transient
    protected org.entur.kingu.model.MultilingualStringEntity description;
    @Transient
    protected org.entur.kingu.model.MultilingualStringEntity note;
    protected Boolean outOfService;

    public org.entur.kingu.model.MultilingualStringEntity getName() {
        return name;
    }

    public void setName(org.entur.kingu.model.MultilingualStringEntity value) {
        this.name = value;
    }

    public org.entur.kingu.model.PrivateCodeStructure getPrivateCode() {
        return privateCode;
    }

    public void setPrivateCode(org.entur.kingu.model.PrivateCodeStructure value) {
        this.privateCode = value;
    }

    public org.entur.kingu.model.PrivateCodeStructure getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(org.entur.kingu.model.PrivateCodeStructure value) {
        this.publicCode = value;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String value) {
        this.image = value;
    }

    public TypeOfEquipmentRefStructure getTypeOfEquipmentRef() {
        return typeOfEquipmentRef;
    }

    public void setTypeOfEquipmentRef(TypeOfEquipmentRefStructure value) {
        this.typeOfEquipmentRef = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getDescription() {
        return description;
    }

    public void setDescription(org.entur.kingu.model.MultilingualStringEntity value) {
        this.description = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getNote() {
        return note;
    }

    public void setNote(org.entur.kingu.model.MultilingualStringEntity value) {
        this.note = value;
    }

    public Boolean isOutOfService() {
        return outOfService;
    }

    public void setOutOfService(Boolean value) {
        this.outOfService = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("netexId", netexId)
                .add("version", version)
                .add("name", name)
                .add("created", created)
                .toString();
    }
}
