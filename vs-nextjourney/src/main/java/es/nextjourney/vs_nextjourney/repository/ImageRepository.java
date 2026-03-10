package es.nextjourney.vs_nextjourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextjourney.vs_nextjourney.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}