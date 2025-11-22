package com.esgbank.greenbond.monitoring.mapper;

import com.esgbank.greenbond.monitoring.dto.ImpactMetricRequest;
import com.esgbank.greenbond.monitoring.dto.ImpactMetricResponse;
import com.esgbank.greenbond.monitoring.dto.DataQualityResponse;
import com.esgbank.greenbond.monitoring.model.ImpactMetric;
import com.esgbank.greenbond.monitoring.model.DataQuality;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImpactMetricMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "metricId", ignore = true)
    @Mapping(target = "dataQuality", ignore = true)
    @Mapping(target = "blockchainTxHash", ignore = true)
    @Mapping(target = "blockchainRecordedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    ImpactMetric toEntity(ImpactMetricRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "metricId", source = "metricId")
    @Mapping(target = "dataQuality", source = "dataQuality")
    ImpactMetricResponse toResponse(ImpactMetric metric);

    DataQualityResponse toDataQualityResponse(DataQuality dataQuality);
}