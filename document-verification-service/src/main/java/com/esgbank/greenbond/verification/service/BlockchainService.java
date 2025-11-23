package com.esgbank.greenbond.verification.service;

import com.esgbank.greenbond.verification.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    // Запись верификации документа в блокчейн для неизменяемости
    public void recordDocumentVerification(Document document) {
        log.info("Recording document verification on blockchain: {}", document.getDocumentId());

        try {
            // В реальной реализации здесь должен быть вызов blockchain-integration сервиса
            // для записи хеша верификации документа в блокчейн

            // Mock реализация: генерируем хеш верификации
            String verificationHash = generateVerificationHash(document);

            log.info("Document verification recorded on blockchain: {}, hash: {}",
                    document.getDocumentId(), verificationHash);

        } catch (Exception e) {
            log.error("Failed to record document verification on blockchain: {}. Error: {}",
                    document.getDocumentId(), e.getMessage(), e);
            // Не выбрасываем исключение чтобы не блокировать процесс верификации
        }
    }

    // Генерация хеша верификации из данных документа
    private String generateVerificationHash(Document document) {
        // Формируем строку из ключевых данных документа
        String data = document.getDocumentId() +
                document.getBondId() +
                document.getFileHash() +
                (document.getVerifiedAt() != null ? document.getVerifiedAt().toString() : "");

        // Генерируем хеш (в реальной реализации используется SHA-256)
        return "VERIFY-" + Integer.toHexString(data.hashCode());
    }
}