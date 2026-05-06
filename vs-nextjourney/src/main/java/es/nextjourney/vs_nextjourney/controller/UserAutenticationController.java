package es.nextjourney.vs_nextjourney.controller;

import es.nextjourney.vs_nextjourney.dto.UserDTO;
import es.nextjourney.vs_nextjourney.dto.UserMapper;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.service.UserService;
import es.nextjourney.vs_nextjourney.security.jwt.TokenType;
import es.nextjourney.vs_nextjourney.security.jwt.AuthResponse;
import es.nextjourney.vs_nextjourney.security.jwt.UserLoginService;
import es.nextjourney.vs_nextjourney.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
public class UserAutenticationController {

    private static final Pattern PASSWORD_POLICY = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).+$"
    );
    private static final Pattern SAFE_INPUT_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._@-]{3,50}$"
    );

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserLoginService userLoginService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserAutenticationController(AuthenticationManager authenticationManager,
                              UserDetailsService userDetailsService,
                              JwtTokenProvider jwtTokenProvider,
                              UserLoginService userLoginService,
                              UserService userService,
                              PasswordEncoder passwordEncoder,
                              UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userLoginService = userLoginService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    // anyone can acces the login endpoint
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @ModelAttribute UserDTO user,
            HttpServletResponse response) {

        if (!isSafeInput(user.username())) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(AuthResponse.Status.FAILURE, "Username inválido"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.username(), user.password())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.username());

            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            response.addCookie(buildTokenCookie(TokenType.ACCESS, accessToken));
            response.addCookie(buildTokenCookie(TokenType.REFRESH, refreshToken));

            return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS,
                "Login exitoso. Tokens creados en cookie."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(AuthResponse.Status.FAILURE, "Credenciales incorrectas", e.getMessage()));
        }
    }

    // anyone can access the register endpoint
    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<?> register(
            @ModelAttribute UserDTO user,
            @RequestPart(value = "imageFile", required = false) MultipartFile file)
            throws IOException, SQLException {

        if (!isSafeInput(user.username())) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(AuthResponse.Status.FAILURE, "Username inválido"));
        }

        if (!isSafeInput(user.email())) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(AuthResponse.Status.FAILURE, "Email inválido"));
        }

        if (!isPasswordPolicyValid(user.password())) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(AuthResponse.Status.FAILURE,
                    "La contraseña debe contener letras, números y un carácter especial"));
        }

        if (userService.usernameExists(user.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthResponse(AuthResponse.Status.FAILURE, "Username ya existe"));
        }

        if (userService.emailExists(user.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthResponse(AuthResponse.Status.FAILURE, "Email ya existe"));
        }
        User user2 = userMapper.toDomain(user);

        user2.setRoles(List.of("USER"));
        user2.setPassword(passwordEncoder.encode(user2.getPassword()));

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setImageFile(new SerialBlob(file.getBytes()));
            image.setContentType(file.getContentType());
            user2.setImage(image);
        }

        userService.saveUser(user2);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AuthResponse(AuthResponse.Status.SUCCESS, "Registro exitoso"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletResponse response,
            @CookieValue(name = "RefreshToken") String refreshToken) {
        return userLoginService.refresh(response, refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        return ResponseEntity.ok(userLoginService.logout(response));
    }

    private Cookie buildTokenCookie(TokenType type, String token) {
        Cookie cookie = new Cookie(type.cookieName, token);
        cookie.setMaxAge((int) type.duration.getSeconds());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    private boolean isPasswordPolicyValid(String password) {
        return password != null && PASSWORD_POLICY.matcher(password).matches();
    }

    private boolean isSafeInput(String input) {
        return input != null && SAFE_INPUT_PATTERN.matcher(input).matches();
    }
}