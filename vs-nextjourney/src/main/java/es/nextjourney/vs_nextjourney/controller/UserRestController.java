package es.nextjourney.vs_nextjourney.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.nextjourney.vs_nextjourney.dto.UserDTO;
import es.nextjourney.vs_nextjourney.dto.UserMapper;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.service.ImageService;
import es.nextjourney.vs_nextjourney.model.User;
import es.nextjourney.vs_nextjourney.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.util.List;
import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;
 
@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    private static final Pattern PASSWORD_POLICY = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).+$");

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    private ImageService imageService;

    public UserRestController(UserService userService,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> profile(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = userService.findByUserName(principal.getName());
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @PutMapping(value = "/profile", consumes = "multipart/form-data")
    public ResponseEntity<?> editProfile(
            @RequestPart("user") User user,
            @RequestPart(value = "imageFile", required = false) MultipartFile file,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            Principal principal) throws IOException, SQLException {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User existing = userService.findByUserName(principal.getName());

        if (!existing.getUsername().equals(user.getUsername())
                && userService.usernameExists(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username ya existe");
        }

        if (!existing.getEmail().equals(user.getEmail())
                && userService.emailExists(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email ya existe");
        }

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setImageFile(new SerialBlob(file.getBytes()));
            image.setContentType(file.getContentType());
            existing.setImage(image);
        }

        if (currentPassword != null && !currentPassword.isBlank()) {

            if (!passwordEncoder.matches(currentPassword, existing.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password incorrecta");
            }

            if (!isPasswordPolicyValid(newPassword)) {
                return ResponseEntity.badRequest().body("Password inválida");
            }

            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("No coinciden");
            }

            existing.setPassword(passwordEncoder.encode(newPassword));
        }

        userService.modifyUser(existing);

        return ResponseEntity.ok(userMapper.toDTO(existing));
    }

    @PostMapping("/profile/image")
    public ResponseEntity<Object> uploadProfileImage(@RequestParam MultipartFile imageFile, Principal principal) throws IOException, SQLException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findByUserName(principal.getName());
        
        Image image = new Image();
        image.setImageFile(new SerialBlob(imageFile.getBytes()));
        image.setContentType(imageFile.getContentType());
        user.setImage(image);
        
        userService.modifyUser(user);

        URI location = fromCurrentContextPath()
                .path("/api/v1/images/{imageId}/media")
                .buildAndExpand(image.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/profile/image")
    public ResponseEntity<Object> deleteProfileImage(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userService.findByUserName(principal.getName());
        
        if (user.getImage() != null) {
            user.setImage(null);
            userService.modifyUser(user);
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.notFound().build();
    }

    
    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteProfile(Principal principal, HttpServletRequest request)
            throws ServletException {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findByUserName(principal.getName());
        userService.deleteById(user.getId());

        request.logout();
 
        return ResponseEntity.ok("Cuenta eliminada");
    }

    private boolean isPasswordPolicyValid(String password) {
        return password != null && PASSWORD_POLICY.matcher(password).matches();
    }
}