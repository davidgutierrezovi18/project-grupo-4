package es.nextjourney.vs_nextjourney.javaClass;
import java.util.List;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;


@Entity(name="UserTable")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 15, nullable = false)
    @Size(min = 1 ,max = 15, message = "Debes introducir un nombre de usuario válido")
    private String name;

    @Column(name = "lastName", length = 15, nullable = false)
    @Size(min = 1 ,max = 15, message = "Debes introducir un apellido válido")
    private String lastName;

    @Column(name = "username", length = 15, unique = true, nullable = false)
    @Size(min = 4 ,max = 15, message = "Debes introducir un nombre de usuario válido (entre 4 y 15 caracteres)")
    private String username;

    @Column(name = "dateOfBirth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    @Email(message = "Debes introducir un correo electrónico válido")
    @Size(max = 50, message = "El correo electrónico no puede tener más de 50 caracteres")
    private String email;

    @Column(name = "password", length = 30, nullable = false)
    @Size(min = 8 ,max = 30, message = "La contraseña debe tener entre 8 y 30 caracteres")
    @Pattern(
    regexp = "^(?=(?:.*[A-Z]){1,})(?=(?:.*[a-z]){1,})(?=(?:.*[@$!%*?&]){1,}).+$",
    message = "La contraseña debe tener al menos una mayúscula, una minúscula, un número y un símbolo especial")
    private String password;

    @Column(name = "image", nullable = true)
    private String image;

    @Transient
    private List<Travel> travel;
    @Transient
    private List<Review> review;

    //CONSTRUCTORS
    public User() {}

    public User(String name, String lastName, String username, LocalDate dateOfBirth, String email, String password, String image) {
        this.name = name;
        this.lastName = lastName;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
        this.image = image;
    }

    //GETTERS Y SETTERS
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

    public String getImage() {
        return image;
    }
    public List<Travel> getTravel() {
        return travel;
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

    public void setImage(String image) {
        this.image = image;
    }
    public void setTravel(List<Travel> travel) {
        this.travel = travel;
    }

    //TO STRING
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", image='" + image + '\'' +
                ", travel=" + travel +
                '}';
    }

}

