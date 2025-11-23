package com.esgbank.greenbond.issuance.controller;

import com.esgbank.greenbond.issuance.dto.BondCreationRequest;
import com.esgbank.greenbond.issuance.dto.BondResponse;
import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import com.esgbank.greenbond.issuance.service.BondService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/bonds")
@RequiredArgsConstructor
@Tag(name = "Bond Management", description = "APIs for managing green bond issuance")
public class BondController {

    private final BondService bondService;

    // Эндпоинт для создания новой зеленой облигации
    @PostMapping
    @Operation(summary = "Create a new green bond", description = "Create a new green bond issuance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bond created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bond data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BondResponse> createBond(
            @Parameter(description = "Bond creation request")
            @Valid @RequestBody BondCreationRequest request,
            @RequestHeader("X-User-Id") String issuerId,
            @RequestHeader("X-User-Name") String issuerName) {

        log.info("REST API: Creating new bond for project: {}, issuer: {}",
                request.getProjectName(), issuerId);

        // Создаем облигацию через сервис
        BondResponse response = bondService.createBond(request, issuerId, issuerName);
        return ResponseEntity.ok(response);
    }

    // Эндпоинт для токенизации облигации на блокчейне
    @PostMapping("/{bondId}/tokenize")
    @Operation(summary = "Tokenize a bond", description = "Tokenize an existing bond on blockchain")
    public ResponseEntity<BondResponse> tokenizeBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @RequestHeader("X-User-Id") String issuerId) {

        log.info("REST API: Tokenizing bond: {}, issuer: {}", bondId, issuerId);

        // Токенизируем облигацию через сервис
        BondResponse response = bondService.tokenizeBond(bondId, issuerId);
        return ResponseEntity.ok(response);
    }

    // Эндпоинт для получения информации об облигации по ID
    @GetMapping("/{bondId}")
    @Operation(summary = "Get bond details", description = "Get detailed information about a bond")
    public ResponseEntity<BondResponse> getBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId) {

        log.debug("REST API: Getting bond details for: {}", bondId);

        // Получаем облигацию через сервис
        BondResponse response = bondService.getBond(bondId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get bonds by issuer", description = "Get paginated list of bonds for an issuer")
    public ResponseEntity<Page<BondResponse>> getBondsByIssuer(
            @RequestHeader("X-User-Id") String issuerId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting bonds for issuer: {}, page: {}", issuerId, pageable.getPageNumber());

        Page<BondResponse> bonds = bondService.getBondsByIssuer(issuerId, pageable);
        return ResponseEntity.ok(bonds);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bonds by status", description = "Get paginated list of bonds by status")
    public ResponseEntity<Page<BondResponse>> getBondsByStatus(
            @Parameter(description = "Bond status") @PathVariable BondStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting bonds with status: {}, page: {}", status, pageable.getPageNumber());

        Page<BondResponse> bonds = bondService.getBondsByStatus(status, pageable);
        return ResponseEntity.ok(bonds);
    }

    @PatchMapping("/{bondId}/status")
    @Operation(summary = "Update bond status", description = "Update the status of a bond")
    public ResponseEntity<BondResponse> updateBondStatus(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "New status") @RequestParam BondStatus status,
            @RequestHeader("X-User-Id") String issuerId) {

        log.info("REST API: Updating bond status: {}, new status: {}, issuer: {}",
                bondId, status, issuerId);

        BondResponse response = bondService.updateBondStatus(bondId, status, issuerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bondId}")
    @Operation(summary = "Delete a bond", description = "Delete a bond (only allowed for DRAFT status)")
    public ResponseEntity<Map<String, String>> deleteBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @RequestHeader("X-User-Id") String issuerId) {

        log.info("REST API: Deleting bond: {}, issuer: {}", bondId, issuerId);

        bondService.deleteBond(bondId, issuerId);

        return ResponseEntity.ok(Map.of(
                "message", "Bond deleted successfully",
                "bondId", bondId,
                "requestId", MDC.get("requestId")
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check bond issuance service health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("REST API: Health check");

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "bond-issuance-service",
                "timestamp", System.currentTimeMillis()
        ));
    }
}