package es.nextjourney.vs_nextjourney.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.nextjourney.vs_nextjourney.dto.TravelDTO;
import es.nextjourney.vs_nextjourney.dto.TravelMapper;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.service.TravelService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/travels")
public class TravelRestController {
    private final TravelService travelService;
    private final TravelMapper travelMapper;

    public TravelRestController(TravelService travelService, TravelMapper travelMapper) {
        this.travelService = travelService;
        this.travelMapper = travelMapper;
    }

    @GetMapping("/")
    public ResponseEntity<List<TravelDTO>> getAllTravels(){
        List<Travel> travels = travelService.findAll();
        return ResponseEntity.ok(travelMapper.toDTOs(travels));
    }
    
}
