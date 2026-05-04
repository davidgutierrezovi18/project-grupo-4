package es.nextjourney.vs_nextjourney.dto;

import org.mapstruct.Mapper;
import es.nextjourney.vs_nextjourney.model.User;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "roles", source = "roles")

    UserDTO toDTO(User user);

    List<UserDTO> toDTOs(Collection<User> users);

    
    User toDomain(UserDTO userDTO);

    
}