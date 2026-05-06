package es.nextjourney.vs_nextjourney.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.nextjourney.vs_nextjourney.repository.UserRepository;
import es.nextjourney.vs_nextjourney.security.jwt.JwtRequestFilter;
import es.nextjourney.vs_nextjourney.security.jwt.JwtTokenProvider;
import es.nextjourney.vs_nextjourney.security.jwt.UnauthorizedHandlerJwt;

@Configuration
@EnableWebSecurity
public class SecurityClass {

    private final UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RepositoryUserDetailsService userDetailService;

    @Autowired
    private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

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
                        .requestMatchers("/admin/**", "/admin_users", "/admin_users/**",
                                "/destinations/*/places/*/edit", "/destinations/*/edit", "/destinations/*/delete",
                                "/destinations/*/places/*/delete")
                        .hasRole("ADMIN")

                        // Authenticated users pages
                        .requestMatchers(
                                "/mytravels", "/travel/**", "/user_profile/**", "/add_place", "/add_place/**",
                                "/destinations/*/add_place", "/add_destination/**",
                                "/my_reviews", "/add-review", "/add-review/**")
                        .authenticated()

                        // Public pages
                        .requestMatchers(
                                "/", "/index",
                                "/destinations", "/destinations/**", "/one_destination",
                                "/sign_in", "/register", "/login", "/loginerror",
                                "/error/403", "/error/404", "/error/500",
                                "/css/**", "/js/**", "/images/**", "/assets/**",
                                "/reviews", "/review/**", "/place_reviews")
                        .permitAll()

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
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error/403"));

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

        http
                .securityMatcher("/api/**")
                .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt))
                .authorizeHttpRequests(authorize -> authorize

                        // Authentication endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()

                        // Public read endpoints used by the site
                        .requestMatchers(HttpMethod.GET, "/api/v1/destinations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/places/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/place-metrics").permitAll()

                        
                        // Authenticated API endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").authenticated()
                        .requestMatchers("/api/v1/travels/**").authenticated()
                        .requestMatchers("/api/v1/users/profile").authenticated()
                        .requestMatchers("/api/v1/users/profile/**").authenticated()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/profile/image").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/profile/*/image").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/profile/image").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/profile/*/image").authenticated()

                        // Admin-only API endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/destinations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/destinations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/destinations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/destinations/*/places/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/destinations/*/places/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/destinations/*/places/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/images/**").hasRole("ADMIN")

                        .anyRequest().denyAll()

                )
                .formLogin(form -> form.disable())
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtRequestFilter(userDetailService, jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}

/*
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.context.annotation.Bean;
 * import org.springframework.context.annotation.Configuration;
 * import org.springframework.core.annotation.Order;
 * import org.springframework.http.HttpMethod;
 * import org.springframework.security.authentication.AuthenticationManager;
 * import
 * org.springframework.security.authentication.dao.DaoAuthenticationProvider;
 * import
 * org.springframework.security.config.annotation.authentication.configuration.
 * AuthenticationConfiguration;
 * import
 * org.springframework.security.config.annotation.web.builders.HttpSecurity;
 * import org.springframework.security.config.annotation.web.configuration.
 * EnableWebSecurity;
 * import org.springframework.security.config.http.SessionCreationPolicy;
 * import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 * import org.springframework.security.crypto.password.PasswordEncoder;
 * import org.springframework.security.web.SecurityFilterChain;
 * import org.springframework.security.web.authentication.
 * UsernamePasswordAuthenticationFilter;
 * import es.nextjourney.vs_nextjourney.security.jwt.JwtRequestFilter;
 * import es.nextjourney.vs_nextjourney.security.jwt.JwtTokenProvider;
 * import es.nextjourney.vs_nextjourney.security.jwt.UnauthorizedHandlerJwt;
 * 
 * @Configuration
 * 
 * @EnableWebSecurity
 * public class SecurityConfig {
 * 
 * @Autowired
 * private JwtTokenProvider jwtTokenProvider;
 * 
 * @Autowired
 * public RepositoryUserDetailsService userDetailService;
 * 
 * @Autowired
 * private UnauthorizedHandlerJwt unauthorizedHandlerJwt;
 * 
 * @Bean
 * public PasswordEncoder passwordEncoder() {
 * return new BCryptPasswordEncoder();
 * }
 * 
 * @Bean
 * public AuthenticationManager
 * authenticationManager(AuthenticationConfiguration authConfig) throws
 * Exception {
 * return authConfig.getAuthenticationManager();
 * }
 * 
 * @Bean
 * public DaoAuthenticationProvider authenticationProvider() {
 * DaoAuthenticationProvider authProvider = new
 * DaoAuthenticationProvider(userDetailService);
 * authProvider.setPasswordEncoder(passwordEncoder());
 * return authProvider;
 * }
 * 
 * @Bean
 * 
 * @Order(1)
 * public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception
 * {
 * http.authenticationProvider(authenticationProvider());
 * 
 * http
 * .securityMatcher("/api/**")
 * .exceptionHandling(handling ->
 * handling.authenticationEntryPoint(unauthorizedHandlerJwt))
 * .authorizeHttpRequests(authorize -> authorize
 * // Endpoints Públicos de la API
 * .requestMatchers(HttpMethod.GET, "/api/v1/destinations/**").permitAll()
 * .requestMatchers(HttpMethod.GET, "/api/v1/places/**").permitAll()
 * // Endpoints Privados (Ajustar según tu lógica)
 * .requestMatchers(HttpMethod.POST, "/api/v1/destinations/**").hasRole("ADMIN")
 * .requestMatchers(HttpMethod.DELETE,
 * "/api/v1/destinations/**").hasRole("ADMIN")
 * .requestMatchers(HttpMethod.POST, "/api/v1/travels/**").hasRole("USER")
 * .anyRequest().authenticated()
 * )
 * .formLogin(form -> form.disable())
 * .csrf(csrf -> csrf.disable())
 * .httpBasic(basic -> basic.disable())
 * .sessionManagement(sess ->
 * sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 * .addFilterBefore(new JwtRequestFilter(userDetailService, jwtTokenProvider),
 * UsernamePasswordAuthenticationFilter.class);
 * 
 * return http.build();
 * }
 * 
 * @Bean
 * 
 * @Order(2)
 * public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception
 * {
 * http.authenticationProvider(authenticationProvider());
 * 
 * http
 * .authorizeHttpRequests(authorize -> authorize
 * .requestMatchers("/").permitAll()
 * .requestMatchers("/index").permitAll()
 * .requestMatchers("/css/**", "/js/**", "/img/**", "/assets/**").permitAll()
 * .requestMatchers("/admin_users/**").hasRole("ADMIN")
 * .anyRequest().permitAll()
 * )
 * .formLogin(form -> form
 * .loginPage("/login")
 * .failureUrl("/loginerror")
 * .defaultSuccessUrl("/")
 * .permitAll()
 * )
 * .logout(logout -> logout
 * .logoutUrl("/logout")
 * .logoutSuccessUrl("/")
 * .permitAll()
 * );
 * 
 * return http.build();
 * }
 * }
 */