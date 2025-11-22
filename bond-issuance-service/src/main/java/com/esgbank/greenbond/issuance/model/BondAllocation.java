package com.esgbank.greenbond.issuance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bond_allocations")
public class BondAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id", nullable = false)
    private Bond bond;

    @Column(nullable = false)
    private String investorId;

    @Column(nullable = false)
    private String investorName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal allocatedAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal allocatedUnits;

    @Column(nullable = false)
    private String allocationStatus;

    @Column
    private String blockchainTokenId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime allocatedAt;

    @Column
    private LocalDateTime settledAt;
}