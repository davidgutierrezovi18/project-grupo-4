package es.nextjourney.vs_nextjourney.dto;

import org.mapstruct.Mapper;
import es.nextjourney.vs_nextjourney.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
}