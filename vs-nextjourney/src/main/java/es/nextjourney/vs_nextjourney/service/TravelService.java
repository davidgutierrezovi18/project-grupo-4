package es.nextjourney.vs_nextjourney.service;

import org.springframework.stereotype.Service;

import es.nextjourney.vs_nextjourney.model.Travel;
import es.nextjourney.vs_nextjourney.repository.TravelRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public List<Travel> findByUserId(long userId) {
        return travelRepository.findByUserTravels_Id(userId);
    }

    public List<Travel> findAllByUser(String username) {
        List<Travel> owned = travelRepository.findByOwnerName(username);
        List<Travel> collaborated = travelRepository.findByUserTravels_Username(username);

        // Join without duplicates
        Set<Travel> result = new HashSet<>();
        result.addAll(owned);
        result.addAll(collaborated);

        return new ArrayList<>(result);
    }

}
