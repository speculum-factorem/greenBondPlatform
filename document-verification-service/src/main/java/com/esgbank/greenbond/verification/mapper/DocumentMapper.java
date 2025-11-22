package com.esgbank.greenbond.verification.mapper;

import com.esgbank.greenbond.verification.dto.DocumentResponse;
import com.esgbank.greenbond.verification.model.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentId", source = "documentId")
    DocumentResponse toResponse(Document document);
}