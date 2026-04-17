package es.nextjourney.vs_nextjourney.dto;

public record ReviewDTO(
    Long id,
    String reviewText,
    int rating,
    String authorName
) {}
