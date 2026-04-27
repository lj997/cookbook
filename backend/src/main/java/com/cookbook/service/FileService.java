package com.cookbook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.url-prefix}")
    private String uploadUrlPrefix;

    public String storeFile(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex);
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        Path targetLocation = Paths.get(uploadPath, datePath).toAbsolutePath().normalize();
        Files.createDirectories(targetLocation);
        
        Path filePath = targetLocation.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return uploadUrlPrefix + datePath + "/" + uniqueFilename;
    }

    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        String relativePath = fileUrl.replace(uploadUrlPrefix, "");
        Path filePath = Paths.get(uploadPath, relativePath).toAbsolutePath().normalize();
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
}
