package com.esgbank.greenbond.issuance.model;

import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import com.esgbank.greenbond.issuance.model.enums.BondType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bonds")
public class Bond {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String bondId;

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false)
    private String projectDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BondType bondType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BondStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalSupply;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal faceValue;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal couponRate;

    @Column(nullable = false)
    private LocalDate maturityDate;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private String issuerId;

    @Column(nullable = false)
    private String issuerName;

    @Column(nullable = false)
    private String projectWalletAddress;

    @Column
    private String verifierReportHash;

    @Column
    private String blockchainTxHash;

    @Column
    private String bondContractAddress;

    @Column
    private String esgStandard;

    @Column
    private String greeniumDetails;

    @Column
    private String useOfProceeds;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (bondId == null) {
            bondId = "BOND-" + System.currentTimeMillis();
        }
        if (issueDate == null) {
            issueDate = LocalDate.now();
        }
    }
}