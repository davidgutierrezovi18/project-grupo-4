package es.nextjourney.vs_nextjourney.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.nextjourney.vs_nextjourney.model.Destination;
import es.nextjourney.vs_nextjourney.repository.DestinationRepository;

@Service
public class DestinationService {

    @Autowired
    private DestinationRepository repository;

    public Optional<Destination> findById(long id) {
        return repository.findById(id);
    }

    public List<Destination> findAll() {
        return repository.findAll();
    }

    public boolean exists(long id) {
        return repository.existsById(id);
    }

    public void save(Destination destination) {
        repository.save(destination);
    }

    public void delete(long id) {
        repository.deleteById(id);
    }
    
    // Aditional method for searching multiple destinations by a list of IDs
    public List<Destination> findAllById(List<Long> ids) {
        return repository.findAllById(ids);
    }

    // Method for getting random destinations
    public List<Destination> getRandomDestinations(int limit) {
        List<Destination> allDestinations = repository.findAll();
        Collections.shuffle(allDestinations);
        return allDestinations.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}