package es.nextjourney.vs_nextjourney.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import es.nextjourney.vs_nextjourney.model.Travel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TravelMapper {

    @Mapping(source = "countries", target = "countries", qualifiedByName = "stringToList")
    @Mapping(source = "cities", target = "cities", qualifiedByName = "stringToList")
    @Mapping(source = "places", target = "places", qualifiedByName = "stringToList")
    @Mapping(source = "emailsColaborators", target = "emailsColaborators", qualifiedByName = "stringToList")
    TravelDTO toDTO(Travel travel);

    List<TravelDTO> toDTOs(Collection<Travel> travels);

    @Mapping(source = "countries", target = "countries", qualifiedByName = "listToString")
    @Mapping(source = "cities", target = "cities", qualifiedByName = "listToString")
    @Mapping(source = "places", target = "places", qualifiedByName = "listToString")
    @Mapping(source = "emailsColaborators", target = "emailsColaborators", qualifiedByName = "listToString")
    Travel toDomain(TravelDTO travelDTO);

    @Named("stringToList")
    default List<String> stringToList(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Named("listToString")
    default String listToString(List<String> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return String.join(", ", value);
    }
}
