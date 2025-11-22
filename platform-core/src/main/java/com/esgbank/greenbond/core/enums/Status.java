package com.esgbank.greenbond.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Common status values")
public enum Status {
    @Schema(description = "Active status")
    ACTIVE,

    @Schema(description = "Inactive status")
    INACTIVE,

    @Schema(description = "Pending approval")
    PENDING,

    @Schema(description = "Approved status")
    APPROVED,

    @Schema(description = "Rejected status")
    REJECTED,

    @Schema(description = "Suspended status")
    SUSPENDED,

    @Schema(description = "Completed status")
    COMPLETED,

    @Schema(description = "Cancelled status")
    CANCELLED,

    @Schema(description = "Expired status")
    EXPIRED,

    @Schema(description = "Draft status")
    DRAFT
}