package es.nextjourney.vs_nextjourney.javaClass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Destination {
    private String name;
    private String description;
    private String image;

    public Destination(String name, String description, String image){
        this.name = name;
        this.description = description;
        this.image = image;
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

    //Image getter
    public String getImage() {
        return image;
    }

    //Image setter
    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Destination [name=" + name + ", description=" + description + ", image=" + image + "]";
    }
}