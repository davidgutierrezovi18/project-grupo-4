package es.nextjourney.dto;

import java.time.LocalDate;
import java.util.List;

public record TravelDTO(
    Long id,
    String title,
    String description,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    String ownerName,
    List<String> countries,
    List<String> cities,
    int rating
) {}