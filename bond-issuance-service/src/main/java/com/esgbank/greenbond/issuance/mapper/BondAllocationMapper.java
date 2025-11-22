package com.esgbank.greenbond.issuance.mapper;

import com.esgbank.greenbond.issuance.dto.BondAllocationResponse;
import com.esgbank.greenbond.issuance.model.BondAllocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BondAllocationMapper {

    @Mapping(source = "bond.bondId", target = "bondId")
    BondAllocationResponse toResponse(BondAllocation allocation);
}