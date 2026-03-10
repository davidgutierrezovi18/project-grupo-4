package es.nextjourney.vs_nextjourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextjourney.vs_nextjourney.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}