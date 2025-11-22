package com.esgbank.greenbond.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
@Schema(description = "Base entity with common fields")
public abstract class BaseEntity extends AuditableEntity {

    @Id
    @GeneratedValue(generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "com.esgbank.greenbond.core.model.id.CustomIdGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "Unique identifier")
    private String id;

    @Version
    @Column(name = "version")
    @Schema(description = "Optimistic locking version")
    private Long version;

    @Column(name = "is_active", nullable = false)
    @Schema(description = "Soft delete flag")
    private Boolean isActive = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", isActive=" + isActive +
                '}';
    }
}