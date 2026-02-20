package es.nextjourney.vs_nextjourney.javaClass;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "reviews")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "place_id", nullable = false)
    private Long placeId;
    @Column(nullable = false)
    private int rating;
    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;
    @Column(name = "photo_url")
    private String photoUrl;
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    public Review() {}

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public int getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public void setRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
public String toString() {
    return "Review{" +
            "id=" + id +
            ", userId=" + userId +
            ", placeId=" + placeId +
            ", rating=" + rating +
            ", createdAt=" + createdAt +
            '}';
}


}

