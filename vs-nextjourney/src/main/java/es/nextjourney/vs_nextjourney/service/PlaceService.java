package es.nextjourney.vs_nextjourney.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.nextjourney.vs_nextjourney.model.Place;
import es.nextjourney.vs_nextjourney.repository.PlaceRepository;

@Service
public class PlaceService {

    @Autowired
    private PlaceRepository repository;

    public Optional<Place> findById(long id) {
        return repository.findById(id);
    }

    public List<Place> findAll() {
        return repository.findAll();
    }
    
    public List<Place> findAllById(List<Long> ids) {
        return repository.findAllById(ids);
    }

    public boolean exists(long id) {
        return repository.existsById(id);
    }

    public void save(Place place) {
        repository.save(place);
    }

    public void delete(long id) {
        repository.deleteById(id);
    }
}