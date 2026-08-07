package com.zhoubyte.scorpioelastic.controller;

import com.zhoubyte.scorpioelastic.common.Result;
import com.zhoubyte.scorpioelastic.dto.aggregation.AgeRangeAggregationRequest;
import com.zhoubyte.scorpioelastic.dto.aggregation.AggregationBucketResponse;
import com.zhoubyte.scorpioelastic.dto.aggregation.FieldTermsAggregationRequest;
import com.zhoubyte.scorpioelastic.service.AggregationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/aggregations")
public class ElasticAggregationController {

    private final AggregationService aggregationService;

    public ElasticAggregationController(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @PostMapping("/users/age-ranges")
    public Result<List<AggregationBucketResponse>> aggregateUserAgeRanges(
            @Valid @RequestBody AgeRangeAggregationRequest request) {
        return Result.success(aggregationService.aggregateUserAgeRanges(request));
    }

    @PostMapping("/dynamic/terms")
    public Result<List<AggregationBucketResponse>> aggregateDynamicTerms(
            @Valid @RequestBody FieldTermsAggregationRequest request) {
        return Result.success(aggregationService.aggregateDynamicTerms(request));
    }
}
