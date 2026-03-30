package es.nextjourney.vs_nextjourney.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

//import java.util.List;
//import java.util.ArrayList;

@Entity
public class Destination {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Destination name
    @Column(name = "name", nullable = false)
    private String name;

    // Destination description
    private String description;

    // Destination country
    private String country;

    // COVER IMAGE
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Image coverImage;

    // PLACES
    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Place> places = new ArrayList<>();

    // REVIEWS
    @OneToMany(mappedBy="destination", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Review> reviews;

    // CONSTRUCTORS
    public Destination() {
    }

    public Destination(String name, String description, Image coverImage, List<Place> places) {
        this.name = name;
        this.description = description;
        this.coverImage = coverImage;
        this.places = new ArrayList<>();
        if (places != null){
            this.places = places;
        }
    }

    public Destination(String name, String description, Image coverImage, List<Place> places, List<Review> reviews) {
        this.name = name;
        this.description = description;
        this.coverImage = coverImage;
        this.places = new ArrayList<>();
        if (places != null){
            this.places = places;
        }
        if (reviews != null){
            this.reviews = reviews;
        }
    }

    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Image getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(Image coverImage) {
        this.coverImage = coverImage;
    }

    public List<Place> getPlaces() {
    return places;
}

public void setPlaces(List<Place> places) {
    this.places = places;
}

    public List<Review> getReviews() {
        return reviews;
    }
     
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    

    // TO STRING
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Destination [id=")
            .append(this.id)
            .append(", name=").append(this.name)
            .append(", description=").append(this.description)
            .append(", places=[");

        // Places names
        for (int i = 0; i < places.size(); i++) {
            sb.append(places.get(i).getName());
            if (i < places.size() - 1){
                sb.append(", ");
            }
        }

        sb.append(", reviews=[");
        // Reviews
        for (int i = 0; i < reviews.size(); i++) {
            sb.append(reviews.get(i).getReviewText());
            if (i < reviews.size() - 1){
                sb.append(", ");
            }
        }
        sb.append("]]");
        return sb.toString();
    }

}