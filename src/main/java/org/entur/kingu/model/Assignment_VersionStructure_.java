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
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.math.BigInteger;


@MappedSuperclass
public abstract class Assignment_VersionStructure_
        extends org.entur.kingu.model.DataManagedObjectStructure {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected org.entur.kingu.model.MultilingualStringEntity name;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected org.entur.kingu.model.MultilingualStringEntity description;

    @Transient
    protected BigInteger order;

    public org.entur.kingu.model.MultilingualStringEntity getName() {
        return name;
    }

    public void setName(org.entur.kingu.model.MultilingualStringEntity value) {
        this.name = value;
    }

    public org.entur.kingu.model.MultilingualStringEntity getDescription() {
        return description;
    }

    public void setDescription(org.entur.kingu.model.MultilingualStringEntity value) {
        this.description = value;
    }

    public BigInteger getOrder() {
        return order;
    }

    public void setOrder(BigInteger value) {
        this.order = value;
    }

}
