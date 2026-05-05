package es.nextjourney.vs_nextjourney.model;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Travel {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "El titulo del viaje es obligatorio")
    @Size(max = 150, message = "El titulo del viaje no puede superar 150 caracteres")
    private String title;

    //COVER IMAGE
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Image coverImage;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate endDate;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La descripcion del viaje es obligatoria")
    @Size(max = 3000, message = "La descripcion no puede superar 3000 caracteres")
    private String description;

    // Travel countries, cities and places
    @Size(max = 500, message = "El campo de paises no puede superar 500 caracteres")
    private String countries;

    @Size(max = 500, message = "El campo de ciudades no puede superar 500 caracteres")
    private String cities;

    @Size(max = 500, message = "El campo de lugares no puede superar 500 caracteres")
    private String places;

    @Column(name = "rating", nullable = false)
    @Min(value = 1, message = "La puntuacion debe estar entre 1 y 5")
    @Max(value = 5, message = "La puntuacion debe estar entre 1 y 5")
    private int rating;

    @Column(name = "comment")
    @Size(max = 3000, message = "El comentario no puede superar 3000 caracteres")
    private String comment;

    //CAROUSEL IMAGE
    @OneToMany(mappedBy="travelImage", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Image> carouselImages;

    // Itinerary file name
    @Column(name = "itinerary_url")
    @Size(max = 255, message = "El nombre del itinerario no puede superar 255 caracteres")
    private String itineraryUrl;

    // Path to save the itinerary file
    @Column(name = "itinerary_path")
    private String itineraryPath;  

    @Column(name = "emails_colaborators")
    @Size(max = 500, message = "El listado de colaboradores no puede superar 500 caracteres")
    private String emailsColaborators;

    // USERS
    @ManyToMany
    @JoinTable(name = "travel_user", joinColumns = @JoinColumn(name = "travel_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> userTravels = new ArrayList<>();


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
        if (user != null) {
            this.userTravels.addAll(user);
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

    public List<User> getUserTravels() {
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
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La puntuacion debe estar entre 1 y 5");
        }
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

    public void setUserTravels(List<User> user) {
        this.userTravels = user;
    }

    public String getItineraryPath() {
        return itineraryPath;
    }

    public void setItineraryPath(String itineraryPath) {
        this.itineraryPath = itineraryPath;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Travel)) return false;
        Travel travel = (Travel) o;
        return id != null && id.equals(travel.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}