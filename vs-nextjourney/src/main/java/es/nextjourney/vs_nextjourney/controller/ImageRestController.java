package es.nextjourney.vs_nextjourney.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.nextjourney.vs_nextjourney.dto.ImageDTO;
import es.nextjourney.vs_nextjourney.dto.ImageMapper;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.service.ImageService;

@RestController
@RequestMapping("/api/v1/images")
public class ImageRestController {

	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageMapper imageMapper;

	@GetMapping("/{id}")
	public ImageDTO getImage(@PathVariable long id) {
		return imageMapper.toDTO(imageService.getImageById(id));
	}

	@GetMapping("/{id}/media")
	public ResponseEntity<Object> getImageFile(@PathVariable long id)
			throws SQLException, IOException {

		Resource imageFile = imageService.getImageFile(id);

		MediaType mediaType = MediaTypeFactory
				.getMediaType(imageFile)
				.orElse(MediaType.IMAGE_JPEG);

		return ResponseEntity
				.ok()
				.contentType(mediaType)
				.body(imageFile);
	}

	@PutMapping("/{id}/media")
	public ResponseEntity<Object> replaceImageFile(@PathVariable long id,
			@RequestParam MultipartFile imageFile) throws IOException {

		imageService.replaceImageFile(id, imageFile.getInputStream());
		return ResponseEntity.noContent().build();
	}

	/* 
	@GetMapping({"", "/"})
    public ResponseEntity<Page<Map<String, Object>>> getAllImages(Pageable pageable) {
        Page<Map<String, Object>> images = imageRepository.findAll(pageable)
                .map(this::toMetadata);
                
        return ResponseEntity.ok(images);
    }
	

	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> getImageMetadata(@PathVariable long id) {
		try {
			Image image = imageService.getImageById(id);
			return ResponseEntity.ok(toMetadata(image));
		} catch (NoSuchElementException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/{id}/file")
	public ResponseEntity<Object> getImageFile(@PathVariable long id) throws SQLException {
		try {
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

			return ResponseEntity
					.ok()
					.contentType(mediaType)
					.body(new InputStreamResource(image.getImageFile().getBinaryStream()));
		} catch (NoSuchElementException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("imageFile") MultipartFile imageFile) {
		if (imageFile == null || imageFile.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}

		try {
			Image createdImage = imageService.createImage(imageFile);
			return ResponseEntity.ok(toMetadata(createdImage));
		} catch (IOException ex) {
			return ResponseEntity.internalServerError().build();
		}
	}

	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteImage(@PathVariable long id) {
		if (!imageRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		imageRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	

	private Map<String, Object> toMetadata(Image image) {
		return Map.of(
				"id", image.getId(),
				"contentType", image.getContentType() != null ? image.getContentType() : "application/octet-stream",
				"active", image.isActive(),
				"hasBinary", image.getImageFile() != null,
				"downloadUrl", "/api/v1/images/" + image.getId() + "/file");
	}*/
}