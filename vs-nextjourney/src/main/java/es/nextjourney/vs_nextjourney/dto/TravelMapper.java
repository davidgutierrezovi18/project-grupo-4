package es.nextjourney.vs_nextjourney.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.nextjourney.vs_nextjourney.model.Travel;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TravelMapper {

    TravelDTO toDTO(Travel travel);

    List<TravelDTO> toDTOs(Collection<Travel> travels);

    Travel toDomain(TravelDTO travelDTO);
}
