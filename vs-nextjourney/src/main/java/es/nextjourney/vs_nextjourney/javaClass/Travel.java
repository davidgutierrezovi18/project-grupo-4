package es.nextjourney.vs_nextjourney.javaClass;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(name = "imageURL", nullable = false)
    private String imageUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "countries", nullable = false)
    private String countries;

    @Column(name = "cities", nullable = false)
    private String cities;

    @Column(name = "places_visited", nullable = false)
    private String placesVisited;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Column(name = "carousel_images_urls", nullable = false)
    private String carouselImagesUrls;

    @Column(name = "itinerary_url", nullable = false)
    private String itineraryUrl;

    @Column(name = "emails_colaborators", nullable = false)
    private String emailsColaborators;

    //CONSTRUCTORS
    public Travel() {}

    public Travel(String title, String imageUrl, LocalDate startDate, LocalDate endDate, String description,
            String countries, String cities,
            String placesVisited, int rating, String comment, String carouselImagesUrls, String itineraryUrl,
            String emailsColaborators) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.countries = countries;
        this.cities = cities;
        this.placesVisited = placesVisited;
        this.rating = rating;
        this.comment = comment;
        this.carouselImagesUrls = carouselImagesUrls;
        this.itineraryUrl = itineraryUrl;
        this.emailsColaborators = emailsColaborators;
    }

    //GETTERS Y SETTERS
    public String getOwnerName() {
        return ownerName;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
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

    public String getCarouselImagesUrls() {
        return carouselImagesUrls;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public void setCarouselImagesUrls(String carouselImagesUrls) {
        this.carouselImagesUrls = carouselImagesUrls;
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
        return "Travel [title=" + title + ", imageUrl=" + imageUrl + ", startDate=" + startDate + ", endDate=" + endDate
                + ", description=" + description + ", countries=" + countries + ", cities=" + cities
                + ", placesVisited=" + placesVisited + ", rating=" + rating + ", comment=" + comment
                + ", carouselImagesUrls=" + carouselImagesUrls + ", itineraryUrl=" + itineraryUrl
                + ", emailsColaborators=" + emailsColaborators + "]";
    }
}
