package com.esgbank.greenbond.core.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Generic mapper interface for common mapping operations
 *
 * @param <D> DTO type
 * @param <E> Entity type
 */
public interface GenericMapper<D, E> {

    E toEntity(D dto);

    D toDto(E entity);

    List<E> toEntity(List<D> dtoList);

    List<D> toDto(List<E> entityList);

    void updateEntityFromDto(D dto, @MappingTarget E entity);
}