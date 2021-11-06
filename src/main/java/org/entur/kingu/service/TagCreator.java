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

package org.entur.kingu.service;

import com.google.common.collect.ImmutableList;
import org.entur.kingu.model.StopPlace;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;


@Service
public class TagCreator {
    private static final String TAG_NAME_REGEX = "^[\\w\\dæøåÆØÅ]*$";
    private static final Pattern TAG_PATTERN = Pattern.compile(TAG_NAME_REGEX, Pattern.UNICODE_CASE);
    public static final List<Class> SUPPORTED_TAGGABLE_TYPES = ImmutableList.of(StopPlace.class);

}
