package es.nextjourney.vs_nextjourney.dto;

import java.time.LocalDate;
import java.util.List;

import es.nextjourney.vs_nextjourney.model.Image;

public record TravelDTO(
    Long id,
    String ownerName,
    String title,
    Image coverImage,
    LocalDate startDate,
    LocalDate endDate,
    String description,
    List<String> countries,
    List<String> cities,
    List<String> places,
    int rating,
    String comment,
    List<Image> carouselImageUrls,
    String itineraryUrl,
    String itineraryPath,
    List<String> emailsColaborators

) {}