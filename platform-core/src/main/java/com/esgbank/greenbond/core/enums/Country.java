package com.esgbank.greenbond.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported countries")
public enum Country {
    @Schema(description = "United States")
    US,

    @Schema(description = "United Kingdom")
    GB,

    @Schema(description = "Germany")
    DE,

    @Schema(description = "France")
    FR,

    @Schema(description = "Japan")
    JP,

    @Schema(description = "Canada")
    CA,

    @Schema(description = "Australia")
    AU,

    @Schema(description = "Switzerland")
    CH,

    @Schema(description = "Singapore")
    SG,

    @Schema(description = "Hong Kong")
    HK,

    @Schema(description = "China")
    CN,

    @Schema(description = "South Korea")
    KR,

    @Schema(description = "Netherlands")
    NL,

    @Schema(description = "Sweden")
    SE,

    @Schema(description = "Norway")
    NO
}