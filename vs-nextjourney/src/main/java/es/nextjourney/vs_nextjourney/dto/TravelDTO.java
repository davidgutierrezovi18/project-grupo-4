package es.nextjourney.vs_nextjourney.dto;

import java.time.LocalDate;
import java.util.List;

public record TravelDTO(
    Long id,
    String ownerName,
    String title,
    ImageDTO coverImage,
    LocalDate startDate,
    LocalDate endDate,
    String description,
    List<String> countries,
    List<String> cities,
    List<String> places,
    int rating,
    String comment,
    List<ImageDTO> carouselImageUrls,
    String itineraryUrl,
    String itineraryPath,
    String itineraryId,
    List<String> emailsColaborators

) {}