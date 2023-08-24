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

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.JAXBElement;

@MappedSuperclass
public class PurposeOfGrouping_ValueStructure
        extends org.entur.kingu.model.TypeOfValue_VersionStructure {
    @Transient
    protected ClassRefs_RelStructure classes;
    @Transient
    protected JAXBElement<? extends org.entur.kingu.model.TypeOfEntity_VersionStructure> typeOfEntity;

    public ClassRefs_RelStructure getClasses() {
        return classes;
    }

    public void setClasses(ClassRefs_RelStructure value) {
    }

    public JAXBElement<? extends org.entur.kingu.model.TypeOfEntity_VersionStructure> getTypeOfEntity() {
        return typeOfEntity;
    }

    public void setTypeOfEntity(JAXBElement<? extends org.entur.kingu.model.TypeOfEntity_VersionStructure> value) {
        this.typeOfEntity = value;
    }

}
