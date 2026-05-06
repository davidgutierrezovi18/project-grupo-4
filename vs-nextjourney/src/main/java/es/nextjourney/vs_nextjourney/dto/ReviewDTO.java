package es.nextjourney.vs_nextjourney.dto;

import java.util.List;

public record ReviewDTO(
    Long id,
    String reviewText,
    int rating,
    String authorName,
    List<ImageDTO> images
) {}
