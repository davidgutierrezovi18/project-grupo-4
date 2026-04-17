package es.nextjourney.vs_nextjourney.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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


    // Used to encode passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
        

    @Bean
    @Order(2)
    SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())) 
            
            .authorizeHttpRequests(auth -> auth

                // Admin-only pages
                .requestMatchers("/admin/**", "/admin_users", "/admin_users/**","/destinations/*/places/*/edit","/destinations/*/edit","/destinations/*/delete","/destinations/*/places/*/delete")
                .hasRole("ADMIN")
                
                // Authenticated users pages
                .requestMatchers(
                    "/mytravels", "/travel/**", "/user_profile/**", "/add_place", "/add_place/**", "/destinations/*/add_place", "/add_destination/**",
                    "/my_reviews", "/add-review", "/add-review/**"
                ).authenticated()

                // Public pages
                .requestMatchers(
                    "/", "/index",
                    "/destinations", "/destinations/**", "/one_destination",
                    "/sign_in", "/register", "/login", "/loginerror",
                    "/error/403", "/error/404", "/error/500",
                    "/css/**", "/js/**", "/images/**", "/assets/**",
                    "/reviews", "/review/**", "/place_reviews"
                ).permitAll()

                .anyRequest().permitAll()
            )
            
            .formLogin(form -> form
                .loginPage("/sign_in")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index", true)
                .failureUrl("/loginerror")
                .permitAll()
            )
            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
            .map(user -> {
               
                List<? extends GrantedAuthority> authorities = user.getRoles() == null 
                    ? List.of() 
                    : user.getRoles().stream()
                        .filter(role -> role != null && !role.isBlank())
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountLocked(user.isBlocked())
                    .build();
            })
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }


    @Bean
	@Order(1)
	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

		http.authenticationProvider(authenticationProvider());

		http.securityMatcher("/api/**");
				

		http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

		// Disable Form login Authentication
		http.formLogin(formLogin -> formLogin.disable());

		// Disable CSRF protection (it is difficult to implement in REST APIs)
		http.csrf(csrf -> csrf.disable());

		// Disable Basic Authentication
		http.httpBasic(httpBasic -> httpBasic.disable());

		// Stateless session
		http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		

		return http.build();
	}

}