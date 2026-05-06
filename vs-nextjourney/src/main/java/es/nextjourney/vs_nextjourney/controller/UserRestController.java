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
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.regex.Pattern;
import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    private static final Pattern PASSWORD_POLICY = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).+$");

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public UserRestController(UserService userService,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @GetMapping({ "/profile", "/profile/{id}" })
    public ResponseEntity<?> profile(@PathVariable(required = false) Long id,
            Principal principal) {

        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User currentUser = userService.findByUserName(principal.getName());

        User user;

        if (id == null) {
            user = currentUser;
        } else {

            // admin or user propietary
            if (!currentUser.isAdminUser() && !currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permissions to view this profile");
            }

            user = userService.findById(id);
        }

        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @PutMapping(value = { "/profile", "/profile/{id}" }, consumes = "multipart/form-data")
    public ResponseEntity<?> editProfile(@PathVariable(required = false) Long id,
            @RequestPart("user") UserDTO user,
            @RequestPart(value = "imageFile", required = false) MultipartFile file,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            Principal principal) throws IOException, SQLException {

        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User currentUser = userService.findByUserName(principal.getName());
        if (id != null && !currentUser.isAdminUser() && !currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You don't have permissions to edit this profile");
        }

        User existing = (id == null)
                ? currentUser
                : userService.findById(id);

        // unique username
        if (!existing.getUsername().equals(user.username())
                && userService.usernameExists(user.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username ya existe");
        }

        // unique email
        if (!existing.getEmail().equals(user.email())
                && userService.emailExists(user.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email ya existe");
        }

        existing.setUsername(user.username());
        existing.setEmail(user.email());

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setImageFile(new SerialBlob(file.getBytes()));
            image.setContentType(file.getContentType());
            existing.setImage(image);
        }

        // password
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

    @PostMapping(value = { "/profile/image", "/profile/{id}/image" })
    public ResponseEntity<Object> uploadProfileImage(@PathVariable(required = false) Long id,
            @RequestParam MultipartFile imageFile, Principal principal) throws IOException, SQLException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User currentUser = userService.findByUserName(principal.getName());
        User user;
        if (id == null) {
            user = currentUser;
        } else {
            if (!currentUser.isAdminUser() && !currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para modificar esta imagen");
            }
            user = userService.findById(id);
        }

        Image image = new Image();
        image.setImageFile(new SerialBlob(imageFile.getBytes()));
        image.setContentType(imageFile.getContentType());
        user.setImage(image);

        userService.modifyUser(user);

        URI location = fromCurrentContextPath()
                .path("/api/v1/images/{imageId}/media")
                .buildAndExpand(image.getId())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body("Imagen subida correctamente");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            Principal principal,
            HttpServletRequest request) {

        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User currentUser = userService.findByUserName(principal.getName());
        User userToDelete = userService.findById(id);

        if (userToDelete == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");

        if (!currentUser.isAdminUser() && !currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos");
        }

        try {
            // Desvincular relaciones y borrar
            if (userToDelete.getReviews() != null) {
                userToDelete.getReviews().clear();
            }

            if (userToDelete.getRoles() != null) {
                userToDelete.getRoles().clear();
            }

            if (userToDelete.getTravels() != null) {
                for (Travel travel : userToDelete.getTravels()) {
                    travel.getUserTravels().remove(userToDelete);
                }
                userToDelete.getTravels().clear();
            }

            userService.modifyUser(userToDelete);
            userService.deleteById(id);

            if (currentUser.getId().equals(id)) {
                request.logout();
            }

            return ResponseEntity.ok("Usuario eliminado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el usuario: " + e.getMessage());
        }
    }
    @DeleteMapping(value = { "/profile/image", "/profile/{id}/image" })
    public ResponseEntity<Object> deleteProfileImage( @PathVariable(required = false) Long id,Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userService.findByUserName(principal.getName());
        User user;
        if (id == null) {
            user = currentUser;
        } else {
            if (!currentUser.isAdminUser() && !currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar esta imagen");
            }
            user = userService.findById(id);
        }
        if (user.getImage() != null) {
            user.setImage(null);
            userService.modifyUser(user);
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.notFound().build();
    }

    private boolean isPasswordPolicyValid(String password) {
        return password != null && PASSWORD_POLICY.matcher(password).matches();
    }
}