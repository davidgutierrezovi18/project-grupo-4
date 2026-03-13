package es.nextjourney.vs_nextjourney.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.User;



@Service
public class UserService{

    @Autowired
    private UserRepository userRepository;

    public void saveUser(User user) {
        userRepository.save(user);
    }
    public List<User> findAll() {
		return userRepository.findAll();
	}

    public void deleteById(long id) {
		userRepository.deleteById(id);
	}

	public void modifyUser(User user) {
		userRepository.save(user);
	}

    public User addImageToUser(long id, Image image) {
		User user = userRepository.findById(id).orElseThrow();
		user.setImage(image);
		userRepository.save(user);

		return user;
	}

	public User findByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
}

	public User findByUserName(String username) {
    return userRepository.findByUserName(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
}

	public User findById(long id) {
		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	}

}
