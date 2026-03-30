package es.nextjourney.vs_nextjourney.model;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Travel {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(name = "owner_id", nullable = false)
    // OwnerName
    private String ownerName;

    //@Column(name = "name", nullable = false)
    // Travel title
    private String title;

    //COVER IMAGE
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Image coverImage;

    //Dates
    private LocalDate startDate;
    private LocalDate endDate;

    //@Column(name = "description", nullable = false, columnDefinition = "TEXT")
    // Travel description
    private String description;

    // Travel countries, cities and places
    private String countries;
    private String cities;
    private String places;

    //@Column(name = "rating", nullable = false)
    //@Min(value = 0, message = "La puntuación debe estar entre 0 y 5")
    //@Max(value = 5, message = "La puntuación debe estar entre 0 y 5")
    // Star rating
    private int rating;

    //@Column(name = "comment")
    // Travel comment
    private String comment;

    //CAROUSEL IMAGE
    @OneToMany(mappedBy="travelImage", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Image> carouselImages;

    //@Column(name = "itinerary_url")
    // Travel itinerary
    private String itineraryUrl;

    //@Column(name = "emails_colaborators")
    // Travel colaborators (emails)
    private String emailsColaborators;

    // USERS
    @ManyToMany
    private List<User> userTravels;


    //CONSTRUCTORS
    public Travel() {}

    //TO STRING
    public Travel(String ownerName, String title, Image coverImage, LocalDate startDate, LocalDate endDate,
              String description, String countries, String cities, String places, int rating, String comment,
              List<Image> carouselImages, String itineraryUrl, String emailsColaborators, List<User> user) {
        this.ownerName = ownerName;
        this.title = title;
        this.coverImage = coverImage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.countries = countries;
        this.cities = cities;
        this.places = places;
        this.rating = rating;
        this.comment = comment;
        this.carouselImages = new ArrayList<>();
        if(carouselImages != null){
            this.carouselImages = carouselImages;
        }
        this.itineraryUrl = itineraryUrl;
        this.emailsColaborators = emailsColaborators;
        this.userTravels = new ArrayList<>();
        if (user != null){
            this.userTravels = user;
        }
    }


    //GETTERS AND SETTERS

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

    public Image getCoverImage() {
        return coverImage;
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

    public String getPlaces() {
        return places;
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
        this.ownerName = ownerName;
    }

    public List<User> getUser() {
        return userTravels;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCoverImage(Image coverImage) {
        this.coverImage = coverImage;
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

    public void setPlaces(String places) {
        this.places = places;
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

    public void setUser(List<User> user) {
        this.userTravels = user;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Travel [id=").append(id)
            .append(", ownerName=").append(ownerName)
            .append(", title=").append(title)
            .append(", startDate=").append(startDate)
            .append(", endDate=").append(endDate)
            .append(", description=").append(description)
            .append(", countries=").append(countries)
            .append(", cities=").append(cities)
            .append(", places=").append(places)
            .append(", rating=").append(rating)
            .append(", comment=").append(comment)
            .append(", itineraryUrl=").append(itineraryUrl)
            .append(", emailsColaborators=").append(emailsColaborators);
            
            sb.append(", users= [");
            for (int i = 0; i < userTravels.size(); i++) {
                sb.append(userTravels.get(i).getName());
                if (i < userTravels.size() - 1)
                    sb.append(", ");
            }
            
            sb.append("]]");
        return sb.toString();
    }

    public List<User> getUserTravels() {
        return userTravels;
    }

}