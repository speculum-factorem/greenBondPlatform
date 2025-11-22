package com.esgbank.greenbond.monitoring.controller;

import com.esgbank.greenbond.monitoring.dto.ImpactGoalRequest;
import com.esgbank.greenbond.monitoring.dto.ImpactGoalResponse;
import com.esgbank.greenbond.monitoring.model.enums.GoalStatus;
import com.esgbank.greenbond.monitoring.service.ImpactGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/impact/goals")
@RequiredArgsConstructor
@Tag(name = "Impact Goals", description = "APIs for managing impact goals and tracking progress")
public class ImpactGoalController {

    private final ImpactGoalService impactGoalService;

    @PostMapping
    @Operation(summary = "Create an impact goal", description = "Create a new impact goal for a bond")
    public ResponseEntity<ImpactGoalResponse> createGoal(
            @Parameter(description = "Impact goal request")
            @Valid @RequestBody ImpactGoalRequest request) {

        log.info("REST API: Creating impact goal for bond: {}, goal: {}",
                request.getBondId(), request.getGoalName());

        ImpactGoalResponse response = impactGoalService.createGoal(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{goalId}")
    @Operation(summary = "Get impact goal", description = "Get detailed information about an impact goal")
    public ResponseEntity<ImpactGoalResponse> getGoal(
            @Parameter(description = "Goal ID") @PathVariable String goalId) {

        log.debug("REST API: Getting impact goal: {}", goalId);

        ImpactGoalResponse response = impactGoalService.getGoal(goalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bond/{bondId}")
    @Operation(summary = "Get goals by bond", description = "Get paginated list of goals for a bond")
    public ResponseEntity<Page<ImpactGoalResponse>> getGoalsByBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting goals for bond: {}, page: {}", bondId, pageable.getPageNumber());

        Page<ImpactGoalResponse> goals = impactGoalService.getGoalsByBond(bondId, pageable);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/bond/{bondId}/status/{status}")
    @Operation(summary = "Get goals by bond and status", description = "Get paginated list of goals for a bond by status")
    public ResponseEntity<Page<ImpactGoalResponse>> getGoalsByBondAndStatus(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Goal status") @PathVariable GoalStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting goals for bond: {}, status: {}, page: {}", bondId, status, pageable.getPageNumber());

        Page<ImpactGoalResponse> goals = impactGoalService.getGoalsByBondAndStatus(bondId, status, pageable);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/bond/{bondId}/upcoming")
    @Operation(summary = "Get upcoming goals", description = "Get list of upcoming goals for a bond")
    public ResponseEntity<List<ImpactGoalResponse>> getUpcomingGoals(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "5") int limit) {

        log.debug("REST API: Getting upcoming goals for bond: {}, limit: {}", bondId, limit);

        List<ImpactGoalResponse> goals = impactGoalService.getUpcomingGoals(bondId, limit);
        return ResponseEntity.ok(goals);
    }

    @PutMapping("/{goalId}")
    @Operation(summary = "Update impact goal", description = "Update an existing impact goal")
    public ResponseEntity<ImpactGoalResponse> updateGoal(
            @Parameter(description = "Goal ID") @PathVariable String goalId,
            @Parameter(description = "Impact goal request") @Valid @RequestBody ImpactGoalRequest request) {

        log.info("REST API: Updating impact goal: {}", goalId);

        ImpactGoalResponse response = impactGoalService.updateGoal(goalId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{goalId}/update-progress")
    @Operation(summary = "Update goal progress", description = "Manually update progress for a goal")
    public ResponseEntity<ImpactGoalResponse> updateGoalProgress(
            @Parameter(description = "Goal ID") @PathVariable String goalId) {

        log.info("REST API: Updating progress for goal: {}", goalId);

        ImpactGoalResponse response = impactGoalService.updateGoalProgress(goalId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{goalId}")
    @Operation(summary = "Delete a goal", description = "Delete an impact goal")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @Parameter(description = "Goal ID") @PathVariable String goalId) {

        log.info("REST API: Deleting impact goal: {}", goalId);

        impactGoalService.deleteGoal(goalId);

        return ResponseEntity.ok(Map.of(
                "message", "Impact goal deleted successfully",
                "goalId", goalId,
                "requestId", MDC.get("requestId")
        ));
    }

    @GetMapping("/bond/{bondId}/dashboard")
    @Operation(summary = "Get goals dashboard", description = "Get dashboard data for goals of a bond")
    public ResponseEntity<Map<String, Object>> getGoalsDashboard(
            @Parameter(description = "Bond ID") @PathVariable String bondId) {

        log.debug("REST API: Getting goals dashboard for bond: {}", bondId);

        Map<String, Object> dashboard = impactGoalService.getGoalsDashboard(bondId);
        return ResponseEntity.ok(dashboard);
    }
}