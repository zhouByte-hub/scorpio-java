package com.zhoubyte.scorpioelastic.dto.aggregation;

import jakarta.validation.Valid;

import java.util.List;

public record AgeRangeAggregationRequest(
        @Valid
        List<AgeRangeRequest> ranges
) {
}
