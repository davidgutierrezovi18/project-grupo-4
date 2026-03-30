package es.nextjourney.vs_nextjourney.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Import our images service
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.service.ImageService;

@Controller
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/images/{id}")
    public ResponseEntity<Object> getImageFile(@PathVariable long id) throws SQLException {
        Image image = imageService.getImageById(id);

        if (image.getImageFile() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = image.getContentType() != null
                    ? MediaType.parseMediaType(image.getContentType())
                    : MediaType.IMAGE_JPEG;
        } catch (Exception ex) {
            mediaType = MediaType.IMAGE_JPEG;
        }

        // Return image with the correct content type
        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(new InputStreamResource(image.getImageFile().getBinaryStream()));
    }
}