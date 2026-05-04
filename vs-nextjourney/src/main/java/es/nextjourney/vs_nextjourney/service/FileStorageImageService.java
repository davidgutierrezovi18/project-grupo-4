package es.nextjourney.vs_nextjourney.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageImageService {
    @Value("${app.upload.dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", ex);
        }
    }

    /**
     * Guarda el archivo en disco conservando el nombre original para auditoría,
     * pero añadiendo un UUID para evitar colisiones.
     * @return la ruta relativa del archivo guardado
     */
    public String storeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        // Conservamos nombre original pero añadimos UUID para evitar sobreescrituras
        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        
        Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
        
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetLocation.toString(); // Guardamos ruta absoluta o relativa según prefieras
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el archivo " + originalFilename, ex);
        }
    }

    public Path getFilePath(String filePath) {
        return Paths.get(filePath);
    }

    public void deleteFile(String filePath) {
        if (filePath != null) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException e) {
                // Log error
            }
        }
    }
}
