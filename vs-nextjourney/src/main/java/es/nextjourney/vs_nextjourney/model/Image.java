package es.nextjourney.vs_nextjourney.model;

import java.sql.Blob;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class Image {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Lob
    private Blob imageFile;

    private String contentType;

    // Relationship for carousel images (travel class)
    @ManyToOne
    private Travel travelImage;

    // Relationship for review images (review class)
    @ManyToOne
    private Review review;

    private boolean active;

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // CONSTRUCTORS
    public Image() {
    }

    public Image(Blob imageFile) {
        this.imageFile = imageFile;
    }

    // GETTERS AND SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Blob getImageFile() {
        return imageFile;
    }

    public void setImageFile(Blob imageFile) {
        this.imageFile = imageFile;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Travel getTravelImage() {
        return travelImage;
    }

    public void setTravelImage(Travel travelImage) {
        this.travelImage = travelImage;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

}