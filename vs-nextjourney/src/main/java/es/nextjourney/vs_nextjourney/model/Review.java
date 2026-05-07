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
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Review {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating", nullable = false)
    @Min(value = 1, message = "La puntuacion debe estar entre 1 y 5")
    @Max(value = 5, message = "La puntuacion debe estar entre 1 y 5")
    private int rating;

    // Review content
    @Lob
    @Column(name = "review_text", columnDefinition = "TEXT")
    @NotBlank(message = "El texto de la reseña es obligatorio")
    @Size(max = 3000, message = "La reseña no puede superar 3000 caracteres")
    private String reviewText;

    // REVIEW IMAGES
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Image> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @NotNull(message = "La fecha de creación es obligatoria")
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
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La puntuacion debe estar entre 1 y 5");
        }
        this.rating = rating;
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

    public void setDestination(Destination destination){
        this.destination = destination;
    }

    public Image getImage() {
        if (this.images != null && !this.images.isEmpty()) {
            return this.images.get(0); // Devuelve la primera de la lista
        }
        return null;
    }

    public void setImage(Image image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        if (image != null) {
            image.setReview(this); // Importante para la base de datos
            this.images.add(image);
        }
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
