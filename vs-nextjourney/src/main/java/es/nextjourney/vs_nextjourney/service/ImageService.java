package es.nextjourney.vs_nextjourney.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.nextjourney.vs_nextjourney.repository.ImageRepository;
import es.nextjourney.vs_nextjourney.model.Image;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    public Image createImage(MultipartFile imageFile) throws IOException {

        Image image = new Image();

        try {
            image.setImageFile(new SerialBlob(imageFile.getBytes()));
            image.setContentType(imageFile.getContentType());
        } catch (Exception e) {
            throw new IOException("Failed to create image", e);
        }

        imageRepository.save(image);

        return image;
    }

    public Image save(Image image) {
        return imageRepository.save(image);
    }

    public Image getImageById(long id) {
        return imageRepository.findById(id).orElseThrow();
    }

    public Resource getImageFile(long id) throws SQLException {

        Image image = imageRepository.findById(id).orElseThrow();

        if (image.getImageFile() != null) {
            return new InputStreamResource(image.getImageFile().getBinaryStream());
        } else {
            throw new RuntimeException("Image file not found");
        }
    }

    public void replaceImageFile(long id, InputStream inputStream) throws IOException {

        Image image = imageRepository.findById(id).orElseThrow();

        try {
            image.setImageFile(new SerialBlob(inputStream.readAllBytes()));
        } catch (Exception e) {
            throw new IOException("Failed to create image", e);
        }

        imageRepository.save(image);
    }

    public void deleteImageById(long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
        }
    }
}
