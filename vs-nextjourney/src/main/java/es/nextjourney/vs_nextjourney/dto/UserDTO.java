package es.nextjourney.vs_nextjourney.dto;

import java.util.List;

public record UserDTO(
    Long id,
    String username,
    String email,
    String password,
    ImageDTO image,
    List<String> roles
) {}