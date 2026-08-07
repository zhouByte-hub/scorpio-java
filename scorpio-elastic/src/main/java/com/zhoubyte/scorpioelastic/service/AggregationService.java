package com.zhoubyte.scorpioelastic.service;

import com.zhoubyte.scorpioelastic.dto.aggregation.AgeRangeAggregationRequest;
import com.zhoubyte.scorpioelastic.dto.aggregation.AggregationBucketResponse;
import com.zhoubyte.scorpioelastic.dto.aggregation.FieldTermsAggregationRequest;

import java.util.List;

public interface AggregationService {

    List<AggregationBucketResponse> aggregateUserAgeRanges(AgeRangeAggregationRequest request);

    List<AggregationBucketResponse> aggregateDynamicTerms(FieldTermsAggregationRequest request);
}
