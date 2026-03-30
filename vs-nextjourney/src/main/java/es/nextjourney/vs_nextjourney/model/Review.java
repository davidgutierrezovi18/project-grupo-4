package es.nextjourney.vs_nextjourney.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Review {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(name = "rating", nullable = false)
    // @Min(value = 0, message = "La puntuación debe estar entre 0 y 5")
    // @Max(value = 5, message = "La puntuación debe estar entre 0 y 5")
    // Review stars
    private int rating;

    // Review content
    @Lob
    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    // REVIEW IMAGES
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Image> images;

    // @Column(name = "created_at", nullable = false)
    // Creation date
    private LocalDate createdAt;

    // User relationship
    @ManyToOne(optional = false)
    private User userReviews;

    // Destination relationship
    @ManyToOne()
    private Destination destination;

    // Places relationship
    @ManyToOne()
    private Place place;
    


    //CONSTRUCTORS
    public Review() {}
    
    public Review(User user, int rating, String reviewText, LocalDate createdAt, List<Image> images, Place place, Destination destination) {
        this.userReviews = user;
        setRating(rating);
        this.reviewText = reviewText;
        this.createdAt = createdAt;
        this.images = new ArrayList<>();
        if (images != null){
            this.images = images;
        }
        this.place = place;
        this.destination = destination;
    }
      
    //GETTERS AND SETTERS
    public Long getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return userReviews;
    }

    public void setUser(User user) {
        this.userReviews = user;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Place getPlace(){
        return place;
    }

    public void setPlace(Place place){
        this.place = place;
    }

    public Destination getDestination(){
        return destination;
    }

    public void setDescription(Destination destination){
        this.destination = destination;
    }

    // TO STRING
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Review [id=").append(id)
          .append(", rating=").append(rating)
          .append(", reviewText=").append(reviewText)
          .append(", createdAt=").append(createdAt)
          .append(", username=").append(userReviews.getName());
          if (destination != null){
            sb.append(", destination=").append(destination);
          }
          if (place!=null){
            sb.append(", place=").append(place);
          }
        sb.append("]");
        return sb.toString();
    }

}
