package com.esgbank.greenbond.verification.service;

import com.esgbank.greenbond.verification.model.AuditTrail;
import com.esgbank.greenbond.verification.repository.AuditTrailRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditTrailRepository auditTrailRepository;
    private final HttpServletRequest request;

    // Логирование действия с документом в аудит трейл
    public void logDocumentAction(String documentId, String action, String performedBy, String description) {
        log.debug("Logging audit trail: {}, action: {}, user: {}", documentId, action, performedBy);

        try {
            // Создаем запись аудит трейла с информацией о действии
            AuditTrail auditTrail = AuditTrail.builder()
                    .documentId(documentId)
                    .action(action)
                    .performedBy(performedBy)
                    .userRole(getUserRole(performedBy)) // Извлекаем роль из заголовков
                    .performedAt(LocalDateTime.now())
                    .description(description)
                    .ipAddress(getClientIp()) // IP адрес клиента
                    .userAgent(request.getHeader("User-Agent")) // User-Agent браузера
                    .build();

            // Сохраняем в MongoDB для аудита
            auditTrailRepository.save(auditTrail);

            log.debug("Audit trail logged successfully: {}", documentId);

        } catch (Exception e) {
            log.error("Failed to log audit trail: {}. Error: {}", documentId, e.getMessage(), e);
        }
    }

    // Логирование детального действия с сохранением старых и новых значений
    public void logDocumentAction(String documentId, String bondId, String action,
                                  String performedBy, String description,
                                  Map<String, Object> oldValues, Map<String, Object> newValues) {
        log.debug("Logging detailed audit trail: {}, action: {}", documentId, action);

        try {
            // Создаем детальную запись аудит трейла с изменениями
            AuditTrail auditTrail = AuditTrail.builder()
                    .documentId(documentId)
                    .bondId(bondId)
                    .action(action)
                    .performedBy(performedBy)
                    .userRole(getUserRole(performedBy))
                    .performedAt(LocalDateTime.now())
                    .description(description)
                    .oldValues(oldValues) // Старые значения для отслеживания изменений
                    .newValues(newValues) // Новые значения
                    .ipAddress(getClientIp())
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            // Сохраняем в MongoDB
            auditTrailRepository.save(auditTrail);

            log.debug("Detailed audit trail logged successfully: {}", documentId);

        } catch (Exception e) {
            log.error("Failed to log detailed audit trail: {}. Error: {}", documentId, e.getMessage(), e);
        }
    }

    // Извлечение роли пользователя из заголовков запроса
    private String getUserRole(String performedBy) {
        // Извлекаем роль из заголовка X-User-Roles (передается из API Gateway)
        String rolesHeader = request.getHeader("X-User-Roles");
        if (rolesHeader != null && !rolesHeader.isEmpty()) {
            return rolesHeader.split(",")[0]; // Возвращаем первую роль
        }
        return "USER";
    }

    // Извлечение реального IP адреса клиента (учитывает прокси)
    private String getClientIp() {
        // Проверяем заголовок X-Forwarded-For (используется за прокси/load balancer)
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        // Если заголовка нет - берем IP напрямую
        return request.getRemoteAddr();
    }
}