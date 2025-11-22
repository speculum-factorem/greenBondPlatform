package com.esgbank.greenbond.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported currencies")
public enum Currency {
    @Schema(description = "US Dollar")
    USD,

    @Schema(description = "Euro")
    EUR,

    @Schema(description = "British Pound")
    GBP,

    @Schema(description = "Japanese Yen")
    JPY,

    @Schema(description = "Swiss Franc")
    CHF,

    @Schema(description = "Canadian Dollar")
    CAD,

    @Schema(description = "Australian Dollar")
    AUD,

    @Schema(description = "Chinese Yuan")
    CNY,

    @Schema(description = "Hong Kong Dollar")
    HKD,

    @Schema(description = "Singapore Dollar")
    SGD
}