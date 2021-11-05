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

public abstract class Place
        extends org.entur.kingu.model.Zone_VersionStructure {

    protected org.entur.kingu.model.TypeOfPlaceRefs_RelStructure placeTypes;

    public Place() {
    }

    public Place(org.entur.kingu.model.EmbeddableMultilingualString name) {
        super(name);
    }

    public org.entur.kingu.model.TypeOfPlaceRefs_RelStructure getPlaceTypes() {
        return placeTypes;
    }

    public void setPlaceTypes(org.entur.kingu.model.TypeOfPlaceRefs_RelStructure value) {
        this.placeTypes = value;
    }

}
