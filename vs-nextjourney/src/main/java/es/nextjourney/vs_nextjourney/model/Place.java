package es.nextjourney.vs_nextjourney.model;

import java.util.List;

import jakarta.persistence.CascadeType;

//import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

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
    @OneToMany(mappedBy="place", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Review> reviews;

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

    public Place(String name, String description, Category category, Review review) {
        this.name = name;
        this.description = description;
        this.category = category;
        if (reviews != null){
            this.reviews.add(review);
        }
    }

    // GETTERS Y SETTERS
;
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
        sb.append("Place [id=").append(id)
            .append(", name=").append(name)
            .append(", description=").append(description)
            .append(", category=").append(category)
            .append(", destinationName=").append(destination.getName());

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