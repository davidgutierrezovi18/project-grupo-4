package es.nextjourney.vs_nextjourney.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.javaClass.Image;
import es.nextjourney.vs_nextjourney.javaClass.User;



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

    public User addImageToUser(long id, Image image) {
		User user = userRepository.findById(id).orElseThrow();
		user.setImage(image);
		userRepository.save(user);

		return user;
	}



}
