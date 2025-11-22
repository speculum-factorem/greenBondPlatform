package com.esgbank.greenbond.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User roles in the platform")
public enum UserRole {
    @Schema(description = "System administrator with full access")
    ROLE_ADMIN,

    @Schema(description = "Bond issuer")
    ROLE_ISSUER,

    @Schema(description = "Investor")
    ROLE_INVESTOR,

    @Schema(description = "ESG auditor/verifier")
    ROLE_AUDITOR,

    @Schema(description = "Regulator")
    ROLE_REGULATOR,

    @Schema(description = "Platform operator")
    ROLE_OPERATOR,

    @Schema(description = "Read-only user")
    ROLE_VIEWER
}