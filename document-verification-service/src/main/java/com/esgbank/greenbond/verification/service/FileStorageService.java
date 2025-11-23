package com.esgbank.greenbond.verification.service;

import com.esgbank.greenbond.verification.config.FileStorageConfig;
import com.esgbank.greenbond.verification.exception.DocumentProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageConfig storageConfig;
    private final Tika tika = new Tika();

    // Валидация загружаемого файла: проверка размера, типа и безопасности
    public void validateFile(MultipartFile file) {
        log.debug("Validating file: {}", file.getOriginalFilename());

        // Проверяем что файл не пустой
        if (file.isEmpty()) {
            throw new DocumentProcessingException("File is empty");
        }

        // Проверяем размер файла (не превышает лимит)
        if (file.getSize() > storageConfig.getMaxFileSize()) {
            throw new DocumentProcessingException(
                    "File size exceeds maximum allowed size: " + storageConfig.getMaxFileSize());
        }

        // Проверяем расширение файла
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedFileType(fileExtension)) {
            throw new DocumentProcessingException(
                    "File type not allowed. Allowed types: " +
                            Arrays.toString(storageConfig.getAllowedFileTypes()));
        }

        // Дополнительная проверка безопасности: определяем реальный MIME тип через Apache Tika
        try {
            String detectedMimeType = tika.detect(file.getBytes());
            log.debug("Detected MIME type: {} for file: {}", detectedMimeType, file.getOriginalFilename());

            // Проверяем что реальный MIME тип безопасен (защита от подмены расширения)
            if (!isSafeMimeType(detectedMimeType)) {
                throw new DocumentProcessingException("Unsafe file type detected: " + detectedMimeType);
            }

        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to detect file type: " + e.getMessage(), e);
        }

        log.debug("File validation passed: {}", file.getOriginalFilename());
    }

    // Сохранение файла на диск в директорию для конкретной облигации
    public String storeFile(MultipartFile file, String bondId) throws IOException {
        log.debug("Storing file: {} for bond: {}", file.getOriginalFilename(), bondId);

        // Создаем директорию для облигации если её нет
        Path uploadPath = Paths.get(storageConfig.getUploadDir(), bondId);
        Files.createDirectories(uploadPath);

        // Генерируем уникальное имя файла (UUID + оригинальное расширение)
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + fileExtension;
        Path filePath = uploadPath.resolve(fileName);

        // Сохраняем файл на диск
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.debug("File stored successfully: {}", filePath);
        return filePath.toString();
    }

    // Удаление файла с диска
    public void deleteFile(String filePath) throws IOException {
        log.debug("Deleting file: {}", filePath);

        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.debug("File deleted successfully: {}", filePath);
        } else {
            log.warn("File not found for deletion: {}", filePath);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedFileType(String fileExtension) {
        return Arrays.stream(storageConfig.getAllowedFileTypes())
                .anyMatch(allowed -> allowed.equalsIgnoreCase(fileExtension));
    }

    private boolean isSafeMimeType(String mimeType) {
        return mimeType != null && (
                mimeType.startsWith("application/pdf") ||
                        mimeType.startsWith("application/msword") ||
                        mimeType.startsWith("application/vnd.openxmlformats-officedocument") ||
                        mimeType.startsWith("image/") ||
                        mimeType.startsWith("text/")
        );
    }
}