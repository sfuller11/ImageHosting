package com.example.ImageHosting.service;

import com.example.ImageHosting.property.FileStorageProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Log4j2
@Service
public class FileService {

    private final Path fileStorageLocation;

    @Autowired
    public FileService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Path.of(fileStorageProperties.getUploadDir());
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException e) {
            log.error("Could not create the directory where the uploaded files will be stored.", e);
        }
    }

    public String storeFile(MultipartFile multipartFile){

        UUID uuid = UUID.randomUUID();
        String filename = uuid.toString() + ".jpg";

        //String filename = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try{
            Path targetLocation = Path.of(fileStorageLocation.toString()).resolve(filename);
            Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e){
            throw new RuntimeException("Could not store file" + filename + ". Please try again!", e);
        }

    }

    public UrlResource getFile(String fileName){
        try{
            Path filePath = Path.of(fileStorageLocation.toString()).resolve(fileName).normalize();
            UrlResource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()){
                return resource;
            } else {
                throw new RuntimeException("File not found" + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("File not found" + fileName, e);
        }
    }
}
