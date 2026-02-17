package es.nextjourney.vs_nextjourney.javaClass;

import java.time.LocalDate;

public class review {

    private Long id;
    private Long userId;
    private Long placeId;
    private int rating;
    private String reviewText;
    private String photoUrl;
    private LocalDate createdAt;

    public review() {}

    // ===== GETTERS =====

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

    // ===== SETTERS =====

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
}

