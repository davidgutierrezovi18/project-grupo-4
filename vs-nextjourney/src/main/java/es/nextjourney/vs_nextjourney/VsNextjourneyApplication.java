package es.nextjourney.vs_nextjourney;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.repository.UserRepository;

@SpringBootApplication
public class VsNextjourneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(VsNextjourneyApplication.class, args);
	}

	@Bean
	CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		// TEMPORALY: delete this block when the default admin already exists in the database
		return args -> {
			if (userRepository.findByUsername("admin").isPresent()) {
				return;
			}

			User admin = new User();
			admin.setName("Administrador");
			admin.setLastName("Sistema");
			admin.setUsername("admin");
			admin.setEmail("admin@nextjourney.local");
			admin.setDateOfBirth(LocalDate.of(2000, 1, 1));
			admin.setPassword(passwordEncoder.encode("grupo4"));
			admin.setRoles(List.of("ADMIN"));

			userRepository.save(admin);
		};
	}

	@Bean
	CommandLineRunner createDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		// TEMPORALY: delete this block when the default user already exists in the database
		return args -> {
			if (userRepository.findByUsername("user1").isPresent()) {
				return;
			}

			User user1 = new User();
			user1.setName("user1");
			user1.setLastName("Sistema");
			user1.setUsername("user1");
			user1.setEmail("user1@nextjourney.local");
			user1.setDateOfBirth(LocalDate.of(2000, 1, 1));
			user1.setPassword(passwordEncoder.encode("grupo4"));
			user1.setRoles(List.of("USER"));

			userRepository.save(user1);
		};
	}

}