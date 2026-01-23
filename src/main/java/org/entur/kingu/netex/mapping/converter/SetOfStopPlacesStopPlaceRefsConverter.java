package org.entur.kingu.netex.mapping.converter;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.entur.kingu.model.StopPlace;
import org.entur.kingu.repository.reference.ReferenceResolver;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.StopPlaceRefStructure;
import org.rutebanken.netex.model.StopPlaceRefs_RelStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Component
public class SetOfStopPlacesStopPlaceRefsConverter extends BidirectionalConverter<Set<StopPlace>, StopPlaceRefs_RelStructure> {

    private static final Logger logger = LoggerFactory.getLogger(SetOfStopPlacesStopPlaceRefsConverter.class);

    @Autowired
    private ReferenceResolver referenceResolver;

    @Override
    public StopPlaceRefs_RelStructure convertTo(Set<StopPlace> stopPlaces, Type<StopPlaceRefs_RelStructure> type, MappingContext mappingContext) {

        if(stopPlaces != null && !stopPlaces.isEmpty()) {
            logger.debug("Mapping set of stop places to netex. stops: {}", stopPlaces.size());
            return new StopPlaceRefs_RelStructure()
                    .withStopPlaceRef(
                        stopPlaces.stream().map(stopPlace -> {
                            StopPlaceRefStructure stopPlaceRefStructure = new StopPlaceRefStructure();
                            stopPlaceRefStructure.withVersion(String.valueOf(stopPlace.getVersion()));
                            stopPlaceRefStructure.withRef(stopPlace.getNetexId());
                            return new ObjectFactory().createStopPlaceRef(stopPlaceRefStructure);
                        })
                        .collect(toList()));
        }
        return null;
    }

    @Override
    public Set<StopPlace> convertFrom(StopPlaceRefs_RelStructure stopPlaceRefs_relStructure, Type<Set<StopPlace>> type, MappingContext mappingContext) {
        // No implementation needed for current use case
        return Collections.emptySet();
    }
}
