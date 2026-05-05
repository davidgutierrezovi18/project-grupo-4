package es.nextjourney.vs_nextjourney.dto;

import org.mapstruct.Mapper;

import es.nextjourney.vs_nextjourney.model.Image;



@Mapper(componentModel = "spring")
public interface ImageMapper {

	ImageDTO toDTO(Image image);
}

