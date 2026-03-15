package es.nextjourney.vs_nextjourney.service;

import org.springframework.stereotype.Service;

import es.nextjourney.vs_nextjourney.model.Image;
import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.repository.TravelRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;


@Service
public class TravelService {
    
    @Autowired
	private TravelRepository travelRepository;

    public void modifyTravel(Travel travel){
        travelRepository.save(travel);
    }

    public void createTravel(Travel travel) {
		travelRepository.save(travel);
	}

    public List<Travel> findAll() {
		return travelRepository.findAll();
	}

    public Optional<Travel> findById(long id) {
		return travelRepository.findById(id);
	}

    public void deleteById(long id) {
		travelRepository.deleteById(id);
	}

    public void save(Travel travel) {
		travelRepository.save(travel);
	}

    public List<Travel> findByOwnerName(String ownerName) {
        return travelRepository.findByOwnerName(ownerName);
    }

}
