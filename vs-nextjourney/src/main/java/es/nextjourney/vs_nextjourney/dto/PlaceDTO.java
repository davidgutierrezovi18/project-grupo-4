package es.nextjourney.vs_nextjourney.dto;

public record PlaceDTO(
    Long id,
    String name,
    String description,
    String category
) {}