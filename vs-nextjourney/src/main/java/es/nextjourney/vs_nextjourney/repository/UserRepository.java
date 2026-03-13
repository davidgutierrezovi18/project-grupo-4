package es.nextjourney.vs_nextjourney.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import es.nextjourney.vs_nextjourney.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    //deleteById
    //findById
    //save
    //findAll
    Optional<User> findByUserName(String userName); 
    
    Optional<User> findByEmail(String email);  


}