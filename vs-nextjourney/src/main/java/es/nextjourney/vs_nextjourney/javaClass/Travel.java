package es.nextjourney.vs_nextjourney.javaClass;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "travels")
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private String ownerName;

    @Column(name = "name", nullable = false)
    private String title;

    @Column(name = "image", nullable = false)
    private Image image;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "countries", nullable = false)
    private String countries;

    @Column(name = "cities")
    private String cities;

    @Column(name = "places_visited")
    private String placesVisited;

    @Column(name = "rating", nullable = false)
    @Min(value = 0, message = "La puntuación debe estar entre 0 y 5")
    @Max(value = 5, message = "La puntuación debe estar entre 0 y 5")
    private int rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "carousel_images")
    private List<Image> carouselImages;

    @Column(name = "itinerary_url")
    private String itineraryUrl;

    @Column(name = "emails_colaborators")
    private String emailsColaborators;

    //CONSTRUCTORS
    public Travel() {}

    public Travel(String title, Image image, LocalDate startDate, LocalDate endDate, String description,
            String countries, String cities,
            String placesVisited, int rating, String comment, List<Image> carouselImages, String itineraryUrl,
            String emailsColaborators) {
        this.title = title;
        this.image = image;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.countries = countries;
        this.cities = cities;
        this.placesVisited = placesVisited;
        this.rating = rating;
        this.comment = comment;
        this.carouselImages = carouselImages;
        this.itineraryUrl = itineraryUrl;
        this.emailsColaborators = emailsColaborators;
    }

    //GETTERS Y SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getTitle() {
        return title;
    }

    public Image getImage() {
        return image;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    public String getCountries() {
        return countries;
    }

    public String getCities() {
        return cities;
    }

    public String getPlacesVisited() {
        return placesVisited;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public List<Image> getCarouselImages() {
        return carouselImages;
    }

    public String getItineraryUrl() {
        return itineraryUrl;
    }

    public String getEmailsColaborators() {
        return emailsColaborators;
    }

    public void setOwnerName(String ownerName) {
        this.title = ownerName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(Image image) {
        this.image = image;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCountries(String countries) {
        this.countries = countries;
    }

    public void setCities(String cities) {
        this.cities = cities;
    }

    public void setPlacesVisited(String placesVisited) {
        this.placesVisited = placesVisited;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCarouselImagesUrls(List<Image> carouselImages) {
        this.carouselImages = carouselImages;
    }

    public void setItineraryUrl(String itineraryUrl) {
        this.itineraryUrl = itineraryUrl;
    }

    public void setEmailsColaborators(String emailsColaborators) {
        this.emailsColaborators = emailsColaborators;
    }

    //TO STRING
    @Override
    public String toString() {
        return "Travel [title=" + title + ", imageUrl=" + image + ", startDate=" + startDate + ", endDate=" + endDate
                + ", description=" + description + ", countries=" + countries + ", cities=" + cities
                + ", placesVisited=" + placesVisited + ", rating=" + rating + ", comment=" + comment
                + ", carouselImagesUrls=" + carouselImages + ", itineraryUrl=" + itineraryUrl
                + ", emailsColaborators=" + emailsColaborators + "]";
    }
}
