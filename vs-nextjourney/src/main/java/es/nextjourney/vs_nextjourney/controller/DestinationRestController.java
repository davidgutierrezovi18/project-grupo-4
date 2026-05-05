package es.nextjourney.vs_nextjourney.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.nextjourney.vs_nextjourney.dto.DestinationDTO;
import es.nextjourney.vs_nextjourney.dto.PlaceDTO;
import es.nextjourney.vs_nextjourney.model.Destination;
import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Place;
import es.nextjourney.vs_nextjourney.service.DestinationService;
import es.nextjourney.vs_nextjourney.service.PlaceService;

@RestController
@RequestMapping("/api/v1/destinations")
public class DestinationRestController {

	@Autowired
	private DestinationService destinationService;

	@Autowired
	private PlaceService placeService;

	@GetMapping({"", "/"})
		public ResponseEntity<Page<DestinationDTO>> getAllDestinations(Pageable pageable) {
			Page<DestinationDTO> destinations = destinationService.findAll(pageable)
					.map(this::toDto); 
			
			return ResponseEntity.ok(destinations);
		}

	@GetMapping("/{id}")
	public ResponseEntity<DestinationDTO> getDestination(@PathVariable Long id) {
		Optional<Destination> destinationOpt = destinationService.findById(id);
		if (destinationOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(toDto(destinationOpt.get()));
	}

	@PostMapping
	public ResponseEntity<DestinationDTO> createDestination(@RequestBody DestinationDTO destinationDTO) {
		if (!isValidDestination(destinationDTO)) {
			return ResponseEntity.badRequest().build();
		}

		Destination destination = new Destination();
		destination.setName(destinationDTO.name().trim());
		destination.setCountry(destinationDTO.country().trim());
		destination.setDescription(destinationDTO.description().trim());

		// Destination requires a cover image. For API creation without image upload,
		// use a placeholder image entity and let web flow replace it when needed.
		Image coverImage = new Image();
		coverImage.setContentType("application/octet-stream");
		destination.setCoverImage(coverImage);

		destinationService.save(destination);
		DestinationDTO created = toDto(destination);
		return ResponseEntity.created(URI.create("/api/v1/destinations/" + destination.getId()))
				.body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<DestinationDTO> updateDestination(@PathVariable Long id, @RequestBody DestinationDTO destinationDTO) {
		if (!isValidDestination(destinationDTO)) {
			return ResponseEntity.badRequest().build();
		}

		Optional<Destination> existingOpt = destinationService.findById(id);
		if (existingOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Destination existing = existingOpt.get();
		existing.setName(destinationDTO.name().trim());
		existing.setCountry(destinationDTO.country().trim());
		existing.setDescription(destinationDTO.description().trim());

		destinationService.save(existing);
		return ResponseEntity.ok(toDto(existing));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDestination(@PathVariable Long id) {
		if (!destinationService.exists(id)) {
			return ResponseEntity.notFound().build();
		}

		destinationService.delete(id);
		return ResponseEntity.noContent().build();
	}


	@GetMapping("/{id}/places")
	public ResponseEntity<List<PlaceDTO>> getPlacesByDestination(@PathVariable Long id) {
		Optional<Destination> destinationOpt = destinationService.findById(id);
		if (destinationOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		List<PlaceDTO> places = destinationOpt.get().getPlaces().stream()
				.map(this::toPlaceDto)
				.toList();

		return ResponseEntity.ok(places);
	}

	@GetMapping("/{id}/places/{placeId}")
	public ResponseEntity<PlaceDTO> getPlace(@PathVariable Long id, @PathVariable Long placeId) {
		Optional<Destination> destinationOpt = destinationService.findById(id);
		if (destinationOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Optional<Place> placeOpt = placeService.findById(placeId);
		if (placeOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Place place = placeOpt.get();
		// Verify that the place belongs to the destination
		if (!place.getDestination().getId().equals(id)) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(toPlaceDto(place));
	}

	@PostMapping("/{id}/places")
	public ResponseEntity<PlaceDTO> createPlace(@PathVariable Long id, @RequestBody PlaceDTO placeDTO) {
		Optional<Destination> destinationOpt = destinationService.findById(id);
		if (destinationOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		if (!isValidPlace(placeDTO)) {
			return ResponseEntity.badRequest().build();
		}

		Destination destination = destinationOpt.get();

		Place place = new Place();
		place.setName(placeDTO.name().trim());
		place.setDescription(placeDTO.description().trim());
		
		try {
			place.setCategory(Place.Category.valueOf(placeDTO.category().trim()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}

		place.setDestination(destination);
		placeService.save(place);

		PlaceDTO created = toPlaceDto(place);
		return ResponseEntity.created(URI.create("/api/v1/destinations/" + id + "/places/" + place.getId()))
				.body(created);
	}

	@PutMapping("/{id}/places/{placeId}")
	public ResponseEntity<PlaceDTO> updatePlace(@PathVariable Long id, @PathVariable Long placeId, @RequestBody PlaceDTO placeDTO) {
		Optional<Destination> destinationOpt = destinationService.findById(id);
		if (destinationOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Optional<Place> placeOpt = placeService.findById(placeId);
		if (placeOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Place place = placeOpt.get();
		// Verify that the place belongs to the destination
		if (!place.getDestination().getId().equals(id)) {
			return ResponseEntity.notFound().build();
		}

		if (!isValidPlace(placeDTO)) {
			return ResponseEntity.badRequest().build();
		}

		place.setName(placeDTO.name().trim());
		place.setDescription(placeDTO.description().trim());

		try {
			place.setCategory(Place.Category.valueOf(placeDTO.category().trim()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}

		placeService.save(place);
		return ResponseEntity.ok(toPlaceDto(place));
	}

	@DeleteMapping("/{id}/places/{placeId}")
	public ResponseEntity<Void> deletePlace(@PathVariable Long id, @PathVariable Long placeId) {
		Optional<Destination> destinationOpt = destinationService.findById(id);
		if (destinationOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Optional<Place> placeOpt = placeService.findById(placeId);
		if (placeOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Place place = placeOpt.get();
		// Verify that the place belongs to the destination
		if (!place.getDestination().getId().equals(id)) {
			return ResponseEntity.notFound().build();
		}

		placeService.delete(placeId);
		return ResponseEntity.noContent().build();
	}

	private DestinationDTO toDto(Destination destination) {
		return new DestinationDTO(
				destination.getId(),
				destination.getName(),
				destination.getCountry(),
				destination.getDescription());
	}

	private boolean isValidDestination(DestinationDTO destinationDTO) {
		if (destinationDTO == null) {
			return false;
		}

		return isValidText(destinationDTO.name(), 120)
				&& isValidText(destinationDTO.country(), 80)
				&& isValidText(destinationDTO.description(), 3000);
	}

	private boolean isValidText(String value, int maxLength) {
		if (value == null) {
			return false;
		}

		String trimmed = value.trim();
		return !trimmed.isBlank() && trimmed.length() <= maxLength;
	}

	private PlaceDTO toPlaceDto(Place place) {
		return new PlaceDTO(
				place.getId(),
				place.getName(),
				place.getDescription(),
				place.getCategory().toString());
	}

	private boolean isValidPlace(PlaceDTO placeDTO) {
		if (placeDTO == null) {
			return false;
		}

		if (!isValidText(placeDTO.name(), 120)
				|| !isValidText(placeDTO.description(), 3000)
				|| placeDTO.category() == null) {
			return false;
		}

		// Validate that the category is a valid enum value
		try {
			Place.Category.valueOf(placeDTO.category().trim());
		} catch (IllegalArgumentException e) {
			return false;
		}

		return true;
	}
}
