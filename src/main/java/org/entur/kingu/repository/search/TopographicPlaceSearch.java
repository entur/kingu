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

package org.entur.kingu.repository.search;

import com.google.common.base.MoreObjects;
import org.entur.kingu.config.VersionValidity;
import org.entur.kingu.exporter.params.SearchObject;


public class TopographicPlaceSearch implements SearchObject {

    private final VersionValidity versionValidity;


    private TopographicPlaceSearch(Builder builder) {
        this.versionValidity = builder.versionValidity;
    }


    public VersionValidity getVersionValidity() {
        return versionValidity;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("versionValidity", versionValidity)
                .toString();
    }

    public static Builder newTopographicPlaceSearchBuilder() {
        return new Builder();
    }

    public static class Builder {

        private VersionValidity versionValidity;

        public Builder versionValidity(VersionValidity versionValidity) {
            this.versionValidity = versionValidity;
            return this;
        }

        public TopographicPlaceSearch build() {
            return new TopographicPlaceSearch(this);
        }
    }
}