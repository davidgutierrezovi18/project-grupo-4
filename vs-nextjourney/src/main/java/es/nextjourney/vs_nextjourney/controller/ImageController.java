package es.nextjourney.vs_nextjourney.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Importamos TU servicio de imágenes
import es.nextjourney.vs_nextjourney.service.ImageService;

@Controller
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/images/{id}")
    public ResponseEntity<Object> getImageFile(@PathVariable long id) throws SQLException {

        // Buscamos el recurso (el archivo) usando el servicio
        Resource imageFile = imageService.getImageFile(id);

        // Intentamos detectar si es PNG, JPEG, etc., basándonos en el nombre del archivo
        MediaType mediaType = MediaTypeFactory
                .getMediaType(imageFile)
                .orElse(MediaType.IMAGE_JPEG);

        // Devolvemos la imagen con el tipo de contenido correcto
        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(imageFile);
    }
}