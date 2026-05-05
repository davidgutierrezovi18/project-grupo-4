package es.nextjourney.vs_nextjourney.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
public class FileStorageService {
    @Value("${app.upload.dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    // creates the directory if it does not exist
    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", ex);
        }
    }

    // saves the file in disk with a unique name to avoid collisions
    public String storeFile(MultipartFile file) {

        // 1. verify if the file is empty
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        // 2. get the original name and clean it
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        if (originalFilename == null || originalFilename.contains("..")) {
            throw new RuntimeException("Nombre de archivo inválido");
        }

        // 3. validates extension (only allow .pdf)
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Solo se permiten archivos PDF");
        }

        // 4. verify MIME type (should be application/pdf)
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new RuntimeException("Tipo de archivo no permitido");
        }

        // 5. verify real header of PDF (%PDF)
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int bytesRead = is.read(header);

            if (bytesRead < 4 || !new String(header).equals("%PDF")) {
                throw new RuntimeException("El archivo no es un PDF válido");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al validar el archivo", e);
        }

        // 6. we mantain the original name, but we add a UUID to avoid collisions
        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        //    build the destination path
        Path targetLocation = this.fileStorageLocation.resolve(storedFilename);

        // 7. save the file
        try {
            // copies the file to disk and returns the path where it is stored
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el archivo " + originalFilename, ex);
        }
    }

    // return the path of a file
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
}

    // deletes a file from disk
    public void deleteFile(String filePath) {
        if (filePath != null) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException e) {
                 throw new RuntimeException("No se pudo eliminar el archivo", e);
            }
        }
    }
}
