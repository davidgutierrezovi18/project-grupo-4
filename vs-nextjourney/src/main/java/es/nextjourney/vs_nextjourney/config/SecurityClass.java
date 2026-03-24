package es.nextjourney.vs_nextjourney.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())) 
            
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/admin/**", "/admin_users", "/admin_users/**","/destinations/*/places/*/edit","/destinations/*/edit","/destinations/*/delete","/destinations/*/places/*/delete").hasRole("ADMIN")
                
                .requestMatchers(
                    "/mytravels", "/travel/**", "/user_profile/**","/add_place","/add_destinstion","/add_place/**","/destinations/*/add_place","/add_destination/**",
                    "/my_reviews", "/add-review", "/add-review/**"
                ).authenticated()

                .requestMatchers(
                    "/", "/index",
                    "/destinations", "/destinations/**", "/one_destination",
                    "/sign_in", "/register", "/login", "/loginerror",
                    "/error403", "/error404", "/error500",
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
                .accessDeniedPage("/error403")
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
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
