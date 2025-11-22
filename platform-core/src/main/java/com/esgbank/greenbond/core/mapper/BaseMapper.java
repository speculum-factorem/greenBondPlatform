package com.esgbank.greenbond.core.mapper;

import com.esgbank.greenbond.core.model.BaseEntity;
import org.mapstruct.Mapping;

/**
 * Base mapper with common mappings for entities extending BaseEntity
 *
 * @param <D> DTO type
 * @param <E> Entity type extending BaseEntity
 */
public interface BaseMapper<D, E extends BaseEntity> extends GenericMapper<D, E> {

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    E toEntity(D dto);

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}