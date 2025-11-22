package com.esgbank.greenbond.issuance.mapper;

import com.esgbank.greenbond.issuance.dto.BondCreationRequest;
import com.esgbank.greenbond.issuance.dto.BondResponse;
import com.esgbank.greenbond.issuance.model.Bond;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BondMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bondId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "issuerId", ignore = true)
    @Mapping(target = "issuerName", ignore = true)
    @Mapping(target = "issueDate", ignore = true)
    @Mapping(target = "blockchainTxHash", ignore = true)
    @Mapping(target = "bondContractAddress", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Bond toEntity(BondCreationRequest request);

    BondResponse toResponse(Bond bond);
}