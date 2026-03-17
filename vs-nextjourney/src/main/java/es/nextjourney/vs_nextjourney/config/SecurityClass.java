package es.nextjourney.vs_nextjourney.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import es.nextjourney.vs_nextjourney.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityClass {

	private final UserRepository userRepository;

	public SecurityClass(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/", "/index", "/faq",
					"/sign_in", "/register", "/login", "/loginerror",
					"/error403", "/error404", "/error500",
					"/css/**", "/js/**", "/images/**", "/assets/**", "/webjars/**",
					"/h2-console/**",
					"/reviews", "/review/**", "/place_reviews", "/api/reviews/place-metrics")
				.permitAll()
				.requestMatchers("/admin/**", "/admin_users").hasRole("ADMIN")
				.requestMatchers(
					"/mytravels", "/travel/**", "/user_profile/**",
					"/my_reviews", "/add-review", "/add-review/**")
				.authenticated()
				.anyRequest().permitAll())
			.formLogin(form -> form
				.loginPage("/sign_in")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl("/index", true)
				.failureUrl("/loginerror")
				.permitAll())
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/index")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.permitAll())
			.headers(headers -> headers
				.frameOptions(frame -> frame.sameOrigin()));

		return http.build();
	}

	@Bean
	UserDetailsService userDetailsService() {
		return username -> userRepository.findByUsername(username)
			.map(user -> {
				List<GrantedAuthority> authorities = user.getRoles() == null
					? List.of()
					: user.getRoles().stream()
						.filter(role -> role != null && !role.isBlank())
						.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
						.map(SimpleGrantedAuthority::new)
						.map(GrantedAuthority.class::cast)
						.toList();

				UserDetails details = User.withUsername(user.getUsername())
					.password(user.getPassword())
					.authorities(authorities)
					.accountExpired(false)
					.accountLocked(false)
					.credentialsExpired(false)
					.disabled(false)
					.build();

				return details;
			})
			.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();
		return new PasswordEncoder() {
			@Override
			public String encode(CharSequence rawPassword) {
				return delegate.encode(rawPassword);
			}

			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				if (encodedPassword == null) {
					return false;
				}
				if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
					return delegate.matches(rawPassword, encodedPassword);
				}
				return encodedPassword.contentEquals(rawPassword);
			}
		};
	}
}
