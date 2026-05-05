package es.nextjourney.vs_nextjourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.nextjourney.vs_nextjourney.model.Travel;

import java.util.Optional;


import java.util.List;

public interface TravelRepository extends JpaRepository<Travel, Long> {

@Query("SELECT DISTINCT t FROM Travel t LEFT JOIN t.userTravels u " +
           "WHERE t.ownerName = :username OR u.username = :username")
    Page<Travel> findByOwnerOrCollaborator(@Param("username") String username, Pageable pageable);

    List<Travel> findByOwnerName(String ownerName);
    List<Travel> findByUserTravels_Username(String username);
    
    Optional<Travel> findByTitle(String title);
    List<Travel> findByUserTravels_Id(Long userId);

}