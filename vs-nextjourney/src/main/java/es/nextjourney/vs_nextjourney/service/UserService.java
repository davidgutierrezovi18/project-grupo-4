package es.nextjourney.vs_nextjourney.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.User;
import java.util.Optional;



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

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public Optional<User> findByUserName(String userName) {
		return userRepository.findByUsername(userName);
	}

	public Optional<User> findById(long id) {
		return userRepository.findById(id);
	}

}
