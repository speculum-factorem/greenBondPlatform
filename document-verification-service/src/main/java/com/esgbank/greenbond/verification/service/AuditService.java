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

    public void logDocumentAction(String documentId, String action, String performedBy, String description) {
        log.debug("Logging audit trail: {}, action: {}, user: {}", documentId, action, performedBy);

        try {
            AuditTrail auditTrail = AuditTrail.builder()
                    .documentId(documentId)
                    .action(action)
                    .performedBy(performedBy)
                    .userRole(getUserRole(performedBy))
                    .performedAt(LocalDateTime.now())
                    .description(description)
                    .ipAddress(getClientIp())
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            auditTrailRepository.save(auditTrail);

            log.debug("Audit trail logged successfully: {}", documentId);

        } catch (Exception e) {
            log.error("Failed to log audit trail: {}. Error: {}", documentId, e.getMessage(), e);
        }
    }

    public void logDocumentAction(String documentId, String bondId, String action,
                                  String performedBy, String description,
                                  Map<String, Object> oldValues, Map<String, Object> newValues) {
        log.debug("Logging detailed audit trail: {}, action: {}", documentId, action);

        try {
            AuditTrail auditTrail = AuditTrail.builder()
                    .documentId(documentId)
                    .bondId(bondId)
                    .action(action)
                    .performedBy(performedBy)
                    .userRole(getUserRole(performedBy))
                    .performedAt(LocalDateTime.now())
                    .description(description)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .ipAddress(getClientIp())
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            auditTrailRepository.save(auditTrail);

            log.debug("Detailed audit trail logged successfully: {}", documentId);

        } catch (Exception e) {
            log.error("Failed to log detailed audit trail: {}. Error: {}", documentId, e.getMessage(), e);
        }
    }

    private String getUserRole(String performedBy) {
        // Extract role from request headers or user context
        String rolesHeader = request.getHeader("X-User-Roles");
        if (rolesHeader != null && !rolesHeader.isEmpty()) {
            return rolesHeader.split(",")[0]; // Return first role
        }
        return "USER";
    }

    private String getClientIp() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}