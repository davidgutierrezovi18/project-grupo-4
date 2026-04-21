package es.nextjourney.vs_nextjourney.security.jwt;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserLoginService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserLoginService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public ResponseEntity<AuthResponse> login(HttpServletResponse response, LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetails user = userDetailsService.loadUserByUsername(loginRequest.getUsername());

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        response.addCookie(createCookie(TokenType.ACCESS, accessToken));
        response.addCookie(createCookie(TokenType.REFRESH, refreshToken));

        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Login correcto"));
    }

    private Cookie createCookie(TokenType type, String token) {
        Cookie cookie = new Cookie(type.cookieName, token);
        cookie.setMaxAge((int) type.duration.getSeconds());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}