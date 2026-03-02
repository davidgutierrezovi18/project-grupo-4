package es.nextjourney.vs_nextjourney.javaClass;

//import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Place {

    public enum Category {
        HISTORIA,
        MUSEO,
        PARQUE,
        MIRADOR,
        PLAZA,
        OTRO
    }

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(name = "name", nullable = false)
    // Place name
    private String name;

    // @Column(name = "description", columnDefinition = "TEXT")
    // Place description
    private String description;

    // @Column(name = "category", nullable = false)
    // Category
    private Category category;

    // @Column (name="reviews")
    // REVIEWS
    // private List<Review> reviews;

    // Destination relationship
    @ManyToOne(optional = false)
    private Destination destination;

    // CONSTRUCTORS
    public Place() {
    }

    public Place(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    // GETTERS Y SETTERS

    public Long getId() {
        return id;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    /*
    public List<Review> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    */

    // TO STRING
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Place [id=").append(id)
            .append(", name=").append(name)
            .append(", description=").append(description)
            .append(", category=").append(category)
            .append(", destinationName=").append(destination.getName())
            .append("]");
        return sb.toString();
    }

}