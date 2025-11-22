package com.esgbank.greenbond.monitoring.repository;

import com.esgbank.greenbond.monitoring.model.ImpactReport;
import com.esgbank.greenbond.monitoring.model.enums.ReportStatus;
import com.esgbank.greenbond.monitoring.model.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImpactReportRepository extends MongoRepository<ImpactReport, String> {

    Optional<ImpactReport> findByReportId(String reportId);

    Page<ImpactReport> findByBondId(String bondId, Pageable pageable);

    Page<ImpactReport> findByBondIdAndReportType(String bondId, ReportType reportType, Pageable pageable);

    Page<ImpactReport> findByBondIdAndStatus(String bondId, ReportStatus status, Pageable pageable);

    @Query("{ 'bondId': ?0, 'reportingPeriodStart': { $gte: ?1 }, 'reportingPeriodEnd': { $lte: ?2 } }")
    List<ImpactReport> findReportsByBondAndPeriod(String bondId, LocalDateTime start, LocalDateTime end);

    @Query(value = "{ 'bondId': ?0 }", sort = "{ 'generatedAt': -1 }")
    List<ImpactReport> findLatestReports(String bondId, Pageable pageable);
}