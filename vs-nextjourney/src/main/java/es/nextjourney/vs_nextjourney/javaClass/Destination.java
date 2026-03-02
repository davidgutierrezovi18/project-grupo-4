package es.nextjourney.vs_nextjourney.javaClass;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

//import java.util.List;
//import java.util.ArrayList;

@Entity
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(name = "name", nullable = false)
    private String name;

    // @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // COVER IMAGE
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Image coverImage;

    // PLACES
    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Place> places = new ArrayList<>();

    // @Column (name="reviews")
    // private List<Review> reviews;

    // CONSTRUCTORS
    /*
     * public Destination() {}
     * public Destination(String name, String description, Image image){
     * this.name = name;
     * this.description = description;
     * this.image = image;
     * }
     * 
     * //GETTERS Y SETTERS
     * //Name getter
     * public Long getId(){
     * return id;
     * }
     * 
     * public String getName() {
     * return name;
     * }
     * 
     * //Name setter
     * public void setName(String name) {
     * this.name = name;
     * }
     * 
     * //Description getter
     * public String getDescription() {
     * return description;
     * }
     * 
     * //Description setter
     * public void setDescription(String description) {
     * this.description = description;
     * }
     * 
     * //Image getter
     * public Image getImage() {
     * return image;
     * }
     * 
     * //Image setter
     * public void setImage(Image image) {
     * this.image = image;
     * }
     * 
     * //Reviews getter
     * public List<Review> getReviews() {
     * return reviews;
     * }
     * 
     * //Reviews setter
     * public void setReviews(List<Review> reviews) {
     * this.reviews = reviews;
     * }
     * 
     * //TO STRING
     * 
     * @Override
     * public String toString() {
     * return "Destination [name=" + name + ", description=" + description +
     * ", image=" + image + "]";
     * }
     */
}