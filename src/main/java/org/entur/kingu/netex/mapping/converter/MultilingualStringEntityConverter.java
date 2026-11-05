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

package org.entur.kingu.netex.mapping.converter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.entur.kingu.model.MultilingualStringEntity;
import org.entur.kingu.netex.mapping.NetexMultilingualStringHelper;
import org.entur.netex2.model.MultilingualString;
import org.springframework.stereotype.Component;

/**
 * Counterpart of {@link MultilingualStringConverter} for {@link MultilingualStringEntity}, kingu's
 * other single value/lang string type (used for frame, equipment, level and path link
 * descriptions, accessibility comments and parking labels). Without it, Orika maps it by
 * convention, which no longer works since netex's {@link MultilingualString} became mixed
 * content in netex2-java-model 3.0.0, and the text is silently exported as an empty element.
 */
@Component
public class MultilingualStringEntityConverter extends BidirectionalConverter<MultilingualStringEntity, MultilingualString> {

    @Override
    public MultilingualString convertTo(MultilingualStringEntity source, Type<MultilingualString> destinationType, MappingContext mappingContext) {
        if (source == null || source.getValue() == null) {
            return null;
        }
        return NetexMultilingualStringHelper.toNetexModel(source.getValue(), source.getLang());
    }

    @Override
    public MultilingualStringEntity convertFrom(MultilingualString source, Type<MultilingualStringEntity> destinationType, MappingContext mappingContext) {
        String value = NetexMultilingualStringHelper.getValue(source);
        if (value == null) {
            return null;
        }
        return new MultilingualStringEntity(value, NetexMultilingualStringHelper.getLang(source));
    }
}
