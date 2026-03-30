package es.nextjourney.vs_nextjourney.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Place {

    public enum Category {
        Historia,
        Museo,
        Parque,
        Mirador,
        Plaza,
        Otro
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
    @ManyToOne(optional = true)
    private Destination destination;

    // CONSTRUCTORS
    public Place() {
        this.reviews = new ArrayList<>();
    }

    public Place(String name, String description, Category category) {
        this();
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Place(String name, String description, Category category, Review review) {
        this(name, description, category);
        if (review != null) {
            review.setPlace(this);
            this.reviews.add(review);
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    
    public List<Review> getReviews() {
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        return reviews;
    }
    
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
    

    // TO STRING
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Place [id=").append(id)
            .append(", name=").append(name)
            .append(", description=").append(description)
            .append(", category=").append(category)
            .append(", destinationName=")
            .append(destination != null ? destination.getName() : "-");

            sb.append(", reviews=[");
            // Reviews
            List<Review> currentReviews = getReviews();
            for (int i = 0; i < currentReviews.size(); i++) {
                sb.append(currentReviews.get(i).getReviewText());
                if (i < currentReviews.size() - 1){
                    sb.append(", ");
                }
            }
            sb.append("]]");
        return sb.toString();
    }

    
        public String getIcon() {
            if (this.category == null) return "bi-geo-alt";

            return switch (this.category) {
                case Historia -> "bi-bank";
                case Museo    -> "bi-palette";
                case Parque   -> "bi-tree";
                case Mirador  -> "bi-sunset";
                case Plaza    -> "bi-building";
                case Otro     -> "bi-geo-alt";
                
            };
        }

}