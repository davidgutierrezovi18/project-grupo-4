package es.nextjourney.vs_nextjourney.javaClass;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(name = "place_id", nullable = false)
    private Long placeId;

    // @Column(name = "rating", nullable = false)
    // @Min(value = 0, message = "La puntuación debe estar entre 0 y 5")
    // @Max(value = 5, message = "La puntuación debe estar entre 0 y 5")
    private int rating;

    // @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    // @Column(name = "photo_url")
    // private Image photoUrl;

    // @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // User relationship
    @ManyToOne(optional = false)
    private User user;

    // @ManyToOne
    // private Destination destination;

    // @ManyToOne
    // private Place place;

    // REVIEW IMAGES
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    /*
     * //CONSTRUCTORS
     * public Review() {}
     * 
     * public Review(Long userId, Long placeId, int rating, String reviewText, Image
     * photoUrl, LocalDate createdAt) {
     * this.userId = userId;
     * this.placeId = placeId;
     * setRating(rating);
     * this.reviewText = reviewText;
     * this.photoUrl = photoUrl;
     * this.createdAt = createdAt;
     * }
     * 
     * //GETTERS Y SETTERS
     * public Long getId() {
     * return id;
     * }
     * 
     * public Long getUserId() {
     * return userId;
     * }
     * 
     * public Long getPlaceId() {
     * return placeId;
     * }
     * 
     * public int getRating() {
     * return rating;
     * }
     * 
     * public String getReviewText() {
     * return reviewText;
     * }
     * 
     * public Image getPhotoUrl() {
     * return photoUrl;
     * }
     * 
     * public LocalDate getCreatedAt() {
     * return createdAt;
     * }
     * 
     * 
     * public void setId(Long id) {
     * this.id = id;
     * }
     * 
     * public void setUserId(Long userId) {
     * this.userId = userId;
     * }
     * 
     * public void setPlaceId(Long placeId) {
     * this.placeId = placeId;
     * }
     * 
     * public void setRating(int rating) {
     * if (rating >= 1 && rating <= 5) {
     * this.rating = rating;
     * }
     * }
     * 
     * public void setReviewText(String reviewText) {
     * this.reviewText = reviewText;
     * }
     * 
     * public void setPhotoUrl(Image photoUrl) {
     * this.photoUrl = photoUrl;
     * }
     * 
     * public void setCreatedAt(LocalDate createdAt) {
     * this.createdAt = createdAt;
     * }
     * 
     * //TO STRING
     * 
     * @Override
     * public String toString() {
     * return "Review{" +
     * "id=" + id +
     * ", userId=" + userId +
     * ", placeId=" + placeId +
     * ", rating=" + rating +
     * ", createdAt=" + createdAt +
     * '}';
     * }
     * 
     */
}
