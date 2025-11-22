package com.esgbank.greenbond.monitoring.service;

import com.esgbank.greenbond.monitoring.dto.ImpactGoalRequest;
import com.esgbank.greenbond.monitoring.dto.ImpactGoalResponse;
import com.esgbank.greenbond.monitoring.exception.ImpactMonitoringException;
import com.esgbank.greenbond.monitoring.exception.GoalNotFoundException;
import com.esgbank.greenbond.monitoring.mapper.ImpactGoalMapper;
import com.esgbank.greenbond.monitoring.model.ImpactGoal;
import com.esgbank.greenbond.monitoring.model.enums.GoalStatus;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import com.esgbank.greenbond.monitoring.repository.ImpactGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImpactGoalService {

    private final ImpactGoalRepository goalRepository;
    private final ImpactGoalMapper goalMapper;
    private final ImpactMetricService metricService;
    private final TimeSeriesService timeSeriesService;

    @Transactional
    public ImpactGoalResponse createGoal(ImpactGoalRequest request) {
        String requestId = MDC.get("requestId");
        log.info("Creating impact goal for bond: {}, goal: {}, requestId: {}",
                request.getBondId(), request.getGoalName(), requestId);

        try {
            // Validate goal data
            validateGoalRequest(request);

            // Check if goal already exists for this bond and metric type
            if (goalRepository.existsByBondIdAndMetricType(request.getBondId(), request.getMetricType())) {
                throw new ImpactMonitoringException(
                        "Goal already exists for bond " + request.getBondId() + " and metric type " + request.getMetricType());
            }

            // Create goal entity
            ImpactGoal goal = goalMapper.toEntity(request);
            goal.setStatus(GoalStatus.NOT_STARTED);
            goal.setCurrentValue(BigDecimal.ZERO);
            goal.setProgressPercentage(BigDecimal.ZERO);

            // Set baseline if not provided
            if (goal.getBaselineValue() == null) {
                goal.setBaselineValue(BigDecimal.ZERO);
            }
            if (goal.getBaselineDate() == null) {
                goal.setBaselineDate(LocalDateTime.now());
            }

            ImpactGoal savedGoal = goalRepository.save(goal);

            log.info("Impact goal created successfully: {}, bond: {}",
                    savedGoal.getGoalId(), request.getBondId());

            return goalMapper.toResponse(savedGoal);

        } catch (ImpactMonitoringException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create impact goal for bond: {}. Error: {}",
                    request.getBondId(), e.getMessage(), e);
            throw new ImpactMonitoringException("Impact goal creation failed: " + e.getMessage(), e);
        }
    }

    public ImpactGoalResponse getGoal(String goalId) {
        log.debug("Fetching impact goal: {}", goalId);

        ImpactGoal goal = goalRepository.findByGoalId(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Impact goal not found: " + goalId));

        return goalMapper.toResponse(goal);
    }

    public Page<ImpactGoalResponse> getGoalsByBond(String bondId, Pageable pageable) {
        log.debug("Fetching goals for bond: {}, page: {}", bondId, pageable.getPageNumber());

        Page<ImpactGoal> goals = goalRepository.findByBondId(bondId, pageable);
        return goals.map(goalMapper::toResponse);
    }

    public Page<ImpactGoalResponse> getGoalsByBondAndStatus(String bondId, GoalStatus status, Pageable pageable) {
        log.debug("Fetching goals for bond: {}, status: {}, page: {}", bondId, status, pageable.getPageNumber());

        Page<ImpactGoal> goals = goalRepository.findByBondIdAndStatus(bondId, status, pageable);
        return goals.map(goalMapper::toResponse);
    }

    public List<ImpactGoalResponse> getGoalsByBondAndMetricType(String bondId, MetricType metricType) {
        log.debug("Fetching goals for bond: {}, metric type: {}", bondId, metricType);

        List<ImpactGoal> goals = goalRepository.findByBondIdAndMetricType(bondId, metricType);
        return goals.stream().map(goalMapper::toResponse).toList();
    }

    public List<ImpactGoalResponse> getUpcomingGoals(String bondId, int limit) {
        log.debug("Fetching upcoming goals for bond: {}, limit: {}", bondId, limit);

        var pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<ImpactGoal> goals = goalRepository.findUpcomingGoals(bondId, pageable);
        return goals.stream().map(goalMapper::toResponse).toList();
    }

    public List<ImpactGoalResponse> getActiveGoals(String bondId) {
        log.debug("Fetching active goals for bond: {}", bondId);

        List<GoalStatus> activeStatuses = List.of(
                GoalStatus.NOT_STARTED,
                GoalStatus.IN_PROGRESS,
                GoalStatus.ON_TRACK,
                GoalStatus.AT_RISK
        );

        List<ImpactGoal> goals = goalRepository.findByBondIdAndStatusIn(bondId, activeStatuses);
        return goals.stream().map(goalMapper::toResponse).toList();
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void updateGoalsProgress() {
        log.info("Starting scheduled goal progress update");

        try {
            List<ImpactGoal> allGoals = goalRepository.findAll();
            int updatedCount = 0;

            for (ImpactGoal goal : allGoals) {
                if (updateGoalProgress(goal)) {
                    goalRepository.save(goal);
                    updatedCount++;
                }
            }

            log.info("Goal progress update completed. Updated {}/{} goals", updatedCount, allGoals.size());

        } catch (Exception e) {
            log.error("Scheduled goal progress update failed: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public ImpactGoalResponse updateGoalProgress(String goalId) {
        log.info("Updating progress for goal: {}", goalId);

        ImpactGoal goal = goalRepository.findByGoalId(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Impact goal not found: " + goalId));

        updateGoalProgress(goal);
        ImpactGoal updatedGoal = goalRepository.save(goal);

        log.info("Goal progress updated: {}, progress: {}%", goalId, updatedGoal.getProgressPercentage());

        return goalMapper.toResponse(updatedGoal);
    }

    @Transactional
    public ImpactGoalResponse updateGoal(String goalId, ImpactGoalRequest request) {
        log.info("Updating impact goal: {}", goalId);

        ImpactGoal goal = goalRepository.findByGoalId(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Impact goal not found: " + goalId));

        try {
            // Validate update request
            validateGoalRequest(request);

            // Update goal fields
            goal.setGoalName(request.getGoalName());
            goal.setDescription(request.getDescription());
            goal.setTargetValue(request.getTargetValue());
            goal.setTargetUnit(request.getTargetUnit());
            goal.setTargetDate(request.getTargetDate());
            goal.setBaselineValue(request.getBaselineValue());
            goal.setBaselineDate(request.getBaselineDate());
            goal.setVerificationMethod(request.getVerificationMethod());
            goal.setReportingFrequency(request.getReportingFrequency());

            if (request.getKpis() != null) {
                goal.setKpis(request.getKpis());
            }

            // Recalculate progress
            updateGoalProgress(goal);

            ImpactGoal updatedGoal = goalRepository.save(goal);
            log.info("Impact goal updated successfully: {}", goalId);

            return goalMapper.toResponse(updatedGoal);

        } catch (Exception e) {
            log.error("Failed to update impact goal: {}. Error: {}", goalId, e.getMessage(), e);
            throw new ImpactMonitoringException("Impact goal update failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteGoal(String goalId) {
        log.info("Deleting impact goal: {}", goalId);

        ImpactGoal goal = goalRepository.findByGoalId(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Impact goal not found: " + goalId));

        try {
            goalRepository.delete(goal);
            log.info("Impact goal deleted successfully: {}", goalId);

        } catch (Exception e) {
            log.error("Failed to delete impact goal: {}. Error: {}", goalId, e.getMessage(), e);
            throw new ImpactMonitoringException("Impact goal deletion failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getGoalsDashboard(String bondId) {
        log.debug("Generating goals dashboard for bond: {}", bondId);

        try {
            List<ImpactGoal> goals = goalRepository.findByBondId(bondId,
                    org.springframework.data.domain.Pageable.unpaged()).getContent();

            long totalGoals = goals.size();
            long achievedGoals = goals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.ACHIEVED || g.getStatus() == GoalStatus.EXCEEDED)
                    .count();
            long atRiskGoals = goals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.AT_RISK || g.getStatus() == GoalStatus.BEHIND_SCHEDULE)
                    .count();
            long onTrackGoals = goals.stream()
                    .filter(g -> g.getStatus() == GoalStatus.ON_TRACK)
                    .count();

            BigDecimal averageProgress = goals.stream()
                    .map(ImpactGoal::getProgressPercentage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(totalGoals > 0 ? totalGoals : 1), 2, RoundingMode.HALF_UP);

            // Calculate upcoming deadlines (goals due in next 30 days)
            LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
            long upcomingDeadlines = goals.stream()
                    .filter(g -> g.getTargetDate().isBefore(thirtyDaysFromNow) &&
                            g.getTargetDate().isAfter(LocalDateTime.now()) &&
                            g.getStatus() != GoalStatus.ACHIEVED &&
                            g.getStatus() != GoalStatus.EXCEEDED)
                    .count();

            return Map.of(
                    "bondId", bondId,
                    "totalGoals", totalGoals,
                    "achievedGoals", achievedGoals,
                    "onTrackGoals", onTrackGoals,
                    "atRiskGoals", atRiskGoals,
                    "upcomingDeadlines", upcomingDeadlines,
                    "averageProgress", averageProgress,
                    "successRate", totalGoals > 0 ?
                            BigDecimal.valueOf(achievedGoals * 100.0 / totalGoals).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                    "onTrackPercentage", totalGoals > 0 ?
                            BigDecimal.valueOf((onTrackGoals + achievedGoals) * 100.0 / totalGoals).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                    "timestamp", LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("Failed to generate goals dashboard for bond: {}. Error: {}", bondId, e.getMessage(), e);
            throw new ImpactMonitoringException("Goals dashboard generation failed: " + e.getMessage(), e);
        }
    }

    public Map<MetricType, List<ImpactGoalResponse>> getGoalsByMetricType(String bondId) {
        log.debug("Grouping goals by metric type for bond: {}", bondId);

        try {
            List<ImpactGoal> goals = goalRepository.findByBondId(bondId,
                    org.springframework.data.domain.Pageable.unpaged()).getContent();

            return goals.stream()
                    .map(goalMapper::toResponse)
                    .collect(java.util.stream.Collectors.groupingBy(ImpactGoalResponse::getMetricType));

        } catch (Exception e) {
            log.error("Failed to group goals by metric type for bond: {}. Error: {}", bondId, e.getMessage(), e);
            throw new ImpactMonitoringException("Goals grouping failed: " + e.getMessage(), e);
        }
    }

    public ImpactGoalResponse updateGoalStatus(String goalId, GoalStatus status, String comment) {
        log.info("Updating goal status: {}, new status: {}", goalId, status);

        ImpactGoal goal = goalRepository.findByGoalId(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Impact goal not found: " + goalId));

        GoalStatus oldStatus = goal.getStatus();
        goal.setStatus(status);

        // Update KPIs with status change information
        if (goal.getKpis() == null) {
            goal.setKpis(new java.util.HashMap<>());
        }
        goal.getKpis().put("statusHistory",
                goal.getKpis().getOrDefault("statusHistory", "") +
                        String.format("[%s] %s -> %s: %s\n",
                                LocalDateTime.now(), oldStatus, status, comment));

        ImpactGoal updatedGoal = goalRepository.save(goal);
        log.info("Goal status updated: {} -> {}", oldStatus, status);

        return goalMapper.toResponse(updatedGoal);
    }

    public List<ImpactGoalResponse> getOverdueGoals() {
        log.debug("Fetching overdue goals");

        LocalDateTime now = LocalDateTime.now();
        List<GoalStatus> overdueStatuses = List.of(GoalStatus.BEHIND_SCHEDULE, GoalStatus.AT_RISK);

        List<ImpactGoal> overdueGoals = goalRepository.findAll().stream()
                .filter(goal -> goal.getTargetDate().isBefore(now) &&
                        overdueStatuses.contains(goal.getStatus()))
                .toList();

        return overdueGoals.stream().map(goalMapper::toResponse).toList();
    }

    public Map<String, Object> getGoalProgressHistory(String goalId, int months) {
        log.debug("Getting progress history for goal: {}, months: {}", goalId, months);

        ImpactGoal goal = goalRepository.findByGoalId(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Impact goal not found: " + goalId));

        try {
            // This would typically query historical progress data
            // For now, return simulated progress history
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusMonths(months);

            Map<String, BigDecimal> progressHistory = new java.util.HashMap<>();
            LocalDateTime current = startDate;

            while (current.isBefore(endDate)) {
                // Simulate progress data - in real implementation, this would come from historical records
                double progress = Math.min(goal.getProgressPercentage().doubleValue() *
                                (java.time.Duration.between(startDate, current).toDays() /
                                        (double) java.time.Duration.between(startDate, endDate).toDays()),
                        goal.getProgressPercentage().doubleValue());

                progressHistory.put(current.toLocalDate().toString(), BigDecimal.valueOf(progress));
                current = current.plusDays(7); // Weekly progress points
            }

            // Add current progress
            progressHistory.put(endDate.toLocalDate().toString(), goal.getProgressPercentage());

            return Map.of(
                    "goalId", goalId,
                    "goalName", goal.getGoalName(),
                    "period", months + " months",
                    "startDate", startDate,
                    "endDate", endDate,
                    "progressHistory", progressHistory,
                    "currentProgress", goal.getProgressPercentage(),
                    "targetProgress", calculateExpectedProgress(goal, startDate, endDate)
            );

        } catch (Exception e) {
            log.error("Failed to get progress history for goal: {}. Error: {}", goalId, e.getMessage(), e);
            throw new ImpactMonitoringException("Progress history retrieval failed: " + e.getMessage(), e);
        }
    }

    private boolean updateGoalProgress(ImpactGoal goal) {
        try {
            // Get current metric value for this goal
            Map<MetricType, BigDecimal> metricsSummary = timeSeriesService.getBondMetricsSummary(goal.getBondId());
            BigDecimal currentValue = metricsSummary.getOrDefault(goal.getMetricType(), BigDecimal.ZERO);

            // Only update if value has changed significantly (to avoid frequent updates)
            if (currentValue.compareTo(goal.getCurrentValue()) == 0) {
                return false;
            }

            goal.setCurrentValue(currentValue);

            // Calculate progress percentage relative to baseline
            BigDecimal targetFromBaseline = goal.getTargetValue().subtract(goal.getBaselineValue());
            BigDecimal currentFromBaseline = currentValue.subtract(goal.getBaselineValue());

            if (targetFromBaseline.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progress = currentFromBaseline
                        .divide(targetFromBaseline, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                goal.setProgressPercentage(progress.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)));
            } else {
                goal.setProgressPercentage(BigDecimal.ZERO);
            }

            // Update goal status
            GoalStatus oldStatus = goal.getStatus();
            GoalStatus newStatus = determineGoalStatus(goal);
            goal.setStatus(newStatus);

            log.debug("Goal progress updated: {} - {}% ({} -> {})",
                    goal.getGoalId(), goal.getProgressPercentage(), oldStatus, newStatus);

            return true;

        } catch (Exception e) {
            log.error("Failed to update progress for goal: {}. Error: {}", goal.getGoalId(), e.getMessage());
            goal.setStatus(GoalStatus.BEHIND_SCHEDULE);
            return true;
        }
    }

    private GoalStatus determineGoalStatus(ImpactGoal goal) {
        LocalDateTime now = LocalDateTime.now();

        // Check if goal is achieved
        if (goal.getProgressPercentage().compareTo(BigDecimal.valueOf(100)) >= 0) {
            return GoalStatus.ACHIEVED;
        }

        // Check if goal is overdue
        if (goal.getTargetDate().isBefore(now)) {
            return GoalStatus.BEHIND_SCHEDULE;
        }

        // Calculate expected progress based on time elapsed
        long totalDays = java.time.Duration.between(goal.getCreatedAt(), goal.getTargetDate()).toDays();
        long daysElapsed = java.time.Duration.between(goal.getCreatedAt(), now).toDays();

        if (totalDays > 0) {
            double expectedProgress = (double) daysElapsed / totalDays * 100;
            double actualProgress = goal.getProgressPercentage().doubleValue();

            if (actualProgress >= expectedProgress * 1.1) {
                return GoalStatus.EXCEEDED;
            } else if (actualProgress >= expectedProgress * 0.9) {
                return GoalStatus.ON_TRACK;
            } else if (actualProgress >= expectedProgress * 0.7) {
                return GoalStatus.AT_RISK;
            } else {
                return GoalStatus.BEHIND_SCHEDULE;
            }
        }

        return GoalStatus.IN_PROGRESS;
    }

    private BigDecimal calculateExpectedProgress(ImpactGoal goal, LocalDateTime startDate, LocalDateTime endDate) {
        long totalDays = java.time.Duration.between(goal.getCreatedAt(), goal.getTargetDate()).toDays();
        long daysElapsed = java.time.Duration.between(startDate, endDate).toDays();

        if (totalDays > 0) {
            return BigDecimal.valueOf((double) daysElapsed / totalDays * 100)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    private void validateGoalRequest(ImpactGoalRequest request) {
        if (request.getTargetValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ImpactMonitoringException("Target value must be positive");
        }

        if (request.getTargetDate().isBefore(LocalDateTime.now())) {
            throw new ImpactMonitoringException("Target date cannot be in the past");
        }

        if (request.getBaselineValue() != null && request.getBaselineValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new ImpactMonitoringException("Baseline value cannot be negative");
        }

        if (request.getBaselineDate() != null && request.getBaselineDate().isAfter(request.getTargetDate())) {
            throw new ImpactMonitoringException("Baseline date cannot be after target date");
        }

        // Validate that target date is reasonable (not too far in the future)
        LocalDateTime maxReasonableDate = LocalDateTime.now().plusYears(10);
        if (request.getTargetDate().isAfter(maxReasonableDate)) {
            throw new ImpactMonitoringException("Target date cannot be more than 10 years in the future");
        }
    }

    public long getGoalCountByBond(String bondId) {
        return goalRepository.findByBondId(bondId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    public long getGoalCountByBondAndStatus(String bondId, GoalStatus status) {
        return goalRepository.findByBondIdAndStatus(bondId, status, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    public boolean hasActiveGoals(String bondId) {
        List<GoalStatus> activeStatuses = List.of(
                GoalStatus.NOT_STARTED, GoalStatus.IN_PROGRESS,
                GoalStatus.ON_TRACK, GoalStatus.AT_RISK
        );
        return !goalRepository.findByBondIdAndStatusIn(bondId, activeStatuses).isEmpty();
    }
}