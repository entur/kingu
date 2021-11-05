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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.time.Instant;

@MappedSuperclass
public class EntityInVersionStructure extends EntityStructure {


    @Transient
    protected String dataSourceRef;
    protected Instant created;
    protected Instant changed;
    @Transient
    protected org.entur.kingu.model.ModificationEnumeration modification;
    protected long version;
    @Transient
    protected org.entur.kingu.model.StatusEnumeration status;
    @Transient
    protected String derivedFromVersionRef;
    @Transient
    protected String compatibleWithVersionFrameVersionRef;
    @Transient
    protected String derivedFromObjectRef;
    @AttributeOverrides({
            @AttributeOverride(name = "fromDate", column = @Column(name = "from_date")),
            @AttributeOverride(name = "toDate", column = @Column(name = "to_date"))
    })
    @Embedded
    private org.entur.kingu.model.ValidBetween validBetween;

    public String getDataSourceRef() {
        return dataSourceRef;
    }


    public void setDataSourceRef(String value) {
        this.dataSourceRef = value;
    }


    public Instant getCreated() {
        return created;
    }


    public void setCreated(Instant value) {
        this.created = value;
    }


    public Instant getChanged() {
        return changed;
    }


    public void setChanged(Instant value) {
        this.changed = value;
    }

    public org.entur.kingu.model.ValidBetween getValidBetween() {
        return validBetween;
    }

    public void setValidBetween(org.entur.kingu.model.ValidBetween validBetween) {
        this.validBetween = validBetween;
    }

    public org.entur.kingu.model.ModificationEnumeration getModification() {
        if (modification == null) {
            return org.entur.kingu.model.ModificationEnumeration.NEW;
        } else {
            return modification;
        }
    }


    public void setModification(org.entur.kingu.model.ModificationEnumeration value) {
        this.modification = value;
    }


    public long getVersion() {
        return version;
    }


    public void setVersion(long value) {
        this.version = value;
    }


    public org.entur.kingu.model.StatusEnumeration getStatus() {
        if (status == null) {
            return org.entur.kingu.model.StatusEnumeration.ACTIVE;
        } else {
            return status;
        }
    }


    public void setStatus(org.entur.kingu.model.StatusEnumeration value) {
        this.status = value;
    }


    public String getDerivedFromVersionRef() {
        return derivedFromVersionRef;
    }


    public void setDerivedFromVersionRef(String value) {
        this.derivedFromVersionRef = value;
    }


    public String getCompatibleWithVersionFrameVersionRef() {
        return compatibleWithVersionFrameVersionRef;
    }


    public void setCompatibleWithVersionFrameVersionRef(String value) {
        this.compatibleWithVersionFrameVersionRef = value;
    }


    public String getDerivedFromObjectRef() {
        return derivedFromObjectRef;
    }


    public void setDerivedFromObjectRef(String value) {
        this.derivedFromObjectRef = value;
    }

}
