package com.esgbank.greenbond.monitoring.repository;

import com.esgbank.greenbond.monitoring.model.ImpactMetric;
import com.esgbank.greenbond.monitoring.model.enums.MetricType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImpactMetricRepository extends MongoRepository<ImpactMetric, String> {

    Optional<ImpactMetric> findByMetricId(String metricId);

    Page<ImpactMetric> findByBondId(String bondId, Pageable pageable);

    Page<ImpactMetric> findByBondIdAndMetricType(String bondId, MetricType metricType, Pageable pageable);

    List<ImpactMetric> findByBondIdAndMetricTypeAndTimestampBetween(
            String bondId, MetricType metricType, LocalDateTime start, LocalDateTime end);

    @Query("{ 'bondId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<ImpactMetric> findMetricsByBondAndTimeRange(String bondId, LocalDateTime start, LocalDateTime end);

    @Query("{ 'bondId': ?0, 'metricType': ?1, 'timestamp': { $gte: ?2, $lte: ?3 } }")
    List<ImpactMetric> findMetricsByBondTypeAndTimeRange(
            String bondId, MetricType metricType, LocalDateTime start, LocalDateTime end);

    @Query(value = "{ 'bondId': ?0, 'metricType': ?1 }", sort = "{ 'timestamp': -1 }")
    List<ImpactMetric> findLatestMetrics(String bondId, MetricType metricType, Pageable pageable);

    @Query(value = "{ 'bondId': ?0 }", fields = "{ 'metricType': 1 }")
    List<ImpactMetric> findDistinctMetricTypesByBond(String bondId);

    long countByBondIdAndMetricType(String bondId, MetricType metricType);

    boolean existsByBondIdAndMetricType(String bondId, MetricType metricType);
}