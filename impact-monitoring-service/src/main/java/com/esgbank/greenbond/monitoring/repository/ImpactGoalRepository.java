package com.esgbank.greenbond.monitoring.repository;

import com.esgbank.greenbond.monitoring.model.ImpactGoal;
import com.esgbank.greenbond.monitoring.model.enums.GoalStatus;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImpactGoalRepository extends MongoRepository<ImpactGoal, String> {

    Optional<ImpactGoal> findByGoalId(String goalId);

    Page<ImpactGoal> findByBondId(String bondId, Pageable pageable);

    Page<ImpactGoal> findByBondIdAndStatus(String bondId, GoalStatus status, Pageable pageable);

    List<ImpactGoal> findByBondIdAndMetricType(String bondId, MetricType metricType);

    @Query("{ 'bondId': ?0, 'status': { $in: ?1 } }")
    List<ImpactGoal> findByBondIdAndStatusIn(String bondId, List<GoalStatus> statuses);

    @Query(value = "{ 'bondId': ?0 }", sort = "{ 'targetDate': 1 }")
    List<ImpactGoal> findUpcomingGoals(String bondId, Pageable pageable);

    boolean existsByBondIdAndMetricType(String bondId, MetricType metricType);
}