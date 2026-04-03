package es.nextjourney.vs_nextjourney.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GenerationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Entity(name = "UsersTable")
public class User {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
	private List<String> roles;

    @Column(name = "name", length = 15, nullable = false)
    // @Size(min = 1 ,max = 15, message = "Debes introducir un nombre de usuario
    // válido")
    // User name
    private String name;

    @Column(name = "lastName", length = 15, nullable = false)
    // @Size(min = 1 ,max = 15, message = "Debes introducir un apellido válido")
    // User lastname
    private String lastName;

    @Column(name = "username", length = 15, unique = true, nullable = false)
    @Size(min = 4 ,max = 15, message = "Debes introducir un nombre de usuario válido (entre 4 y 15 caracteres)")
    // User username
    private String username;

    @Column(name = "dateOfBirth", nullable = false)
    // User date birth
    private LocalDate dateOfBirth;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    @Email(message = "Debes introducir un correo electrónico válido")
    // @Size(max = 50, message = "El correo electrónico no puede tener más de 50
    // caracteres")
    // User email
    private String email;

    // @Column(name = "password", length = 30, nullable = false)
    // @Size(min = 8 ,max = 30, message = "La contraseña debe tener entre 8 y 30
    // caracteres")
    // @Pattern(
    // regexp = "^(?=(?:.*[A-Z]){1,})(?=(?:.*[a-z]){1,})(?=(?:.*[@$!%*?&]){1,}).+$",
    // message = "La contraseña debe tener al menos una mayúscula, una minúscula, un
    // número y un símbolo especial")
    // User password
    private String password;

    // Account status controlled by administrators
    private boolean blocked;

    // PROFILE IMAGE
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Image image;

    // TRAVELS
    @ManyToMany(mappedBy = "userTravels")
    private List<Travel> travels;

    // REVIEWS
    @OneToMany(mappedBy = "userReviews", cascade = CascadeType.ALL, orphanRemoval = true)//
    private List<Review> reviews;

    
    //CONSTRUCTORS
    public User() {}
    
    public User(String name ,String lastName, String username, LocalDate dateOfBirth, String email, String password, Image image, List<Travel> travels, List<Review> reviews, String... roles) {
        this.name = name;
        this.roles = new ArrayList<>();
        if (roles != null) {
            this.roles.addAll(Arrays.asList(roles));
        }
        this.lastName = lastName;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
        this.image = image;
        this.travels = new ArrayList<>();
        if (travels != null){
            this.travels = travels;
        }
        this.reviews = new ArrayList<>();
        if (reviews != null){
            this.reviews = reviews;
        }
    }
    
    //GETTERS AND SETTERS
    public List<String> getRoles() {
		return roles;
	}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isAdminUser() {
        if (roles == null) {
            return false;
        }
        return roles.stream().anyMatch(role -> "ADMIN".equals(role) || "ROLE_ADMIN".equals(role));
    }

    public Image getImage() {
        return image;
    }

    public List<Travel> getTravels() {
        return travels;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setRoles(List<String> roles) {
		this.roles = roles == null ? null : new ArrayList<>(roles);
	}

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setTravels(List<Travel> travels) {
        this.travels = travels;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    
    
    // TO STRING
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User [id=").append(id)
        .append(", name=").append(name)
        .append(", lastName=").append(lastName)
        .append(", username=").append(username)
        .append(", dateOfBirth=").append(dateOfBirth)
        .append(", email=").append(email)
        .append(", password=").append(password)
        
        .append(", travels= [");
        for (int i = 0; i < travels.size(); i++) {
            sb.append(travels.get(i).getTitle());
            if (i < travels.size() - 1)
                sb.append(", ");
        }

        sb.append("], reviews= [");
        for (int i = 0; i < reviews.size(); i++) {
            sb.append(reviews.get(i).getReviewText());
            if (i < reviews.size() - 1)
                sb.append(", ");
        }
        
        sb.append("]]");
        return sb.toString();
    }
    
}
