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

    // Relationship for carousel images (travel class)
    @ManyToOne
    private Travel travelImage;

    // Relationship for review images (review class)
    @ManyToOne
    private Review review;


    // CONSTRUCTORS
    public Image() {
    }

    public Image(Blob imageFile) {
        this.imageFile = imageFile;
    }

    // GETTERS Y SETTERS
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

}