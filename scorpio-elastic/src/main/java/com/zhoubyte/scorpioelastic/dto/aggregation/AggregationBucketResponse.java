package com.zhoubyte.scorpioelastic.dto.aggregation;

public record AggregationBucketResponse(
        String key,
        Long count
) {
}
