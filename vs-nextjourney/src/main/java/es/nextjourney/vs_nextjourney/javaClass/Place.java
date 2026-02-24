package es.nextjourney.vs_nextjourney.javaClass;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "places")
public class Place {

    public enum Category {
            HISTORIA,
            MUSEO,
            PARQUE,
            MIRADOR,
            PLAZA,
            OTRO
        }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "category", nullable = false)
    private Category category;

    @Column (name="reviews")
    private List<Review> reviews;

    //CONSTRUCTORS
    public Place() {}

    public Place(String name, String description, Category category){
        this.name = name;
        this.description = description;
        this.category = category;
    }

    //GETTERS Y SETTERS

    //Id getter
    public Long getId(){
        return id;
    }

    //Name getter
    public String getName() {
        return name;
    }

    //Name setter
    public void setName(String name) {
        this.name = name;
    }

    //Description getter
    public String getDescription() {
        return description;
    }

    //Description setter
    public void setDescription(String description) {
        this.description = description;
    }

    //Category getter
    public Category getCategory() {
        return category;
    }

    //Category setter
    public void setCategory(Category category) {
        this.category = category;
    }

    //Reviews getter
    public List<Review> getReviews() {
        return reviews;
    }

    //Reviews setter
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    //TO STRING
    @Override
    public String toString() {
        return "Place [name=" + name + ", description=" + description + ", category=" + category + "]";
    }

}
