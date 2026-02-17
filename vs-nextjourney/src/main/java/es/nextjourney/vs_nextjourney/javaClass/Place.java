package es.nextjourney.vs_nextjourney.javaClass;

public class Place {

    public enum Category {
            HISTORIA,
            MUSEO,
            PARQUE,
            MIRADOR,
            PLAZA,
            OTRO
        }

    private String name;
    private String description;
    private Category category;

    public Place(String name, String description, Category category){
        this.name = name;
        this.description = description;
        this.category = category;
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

    //toString
    @Override
    public String toString() {
        return "Place [name=" + name + ", description=" + description + ", category=" + category + "]";
    }

}
