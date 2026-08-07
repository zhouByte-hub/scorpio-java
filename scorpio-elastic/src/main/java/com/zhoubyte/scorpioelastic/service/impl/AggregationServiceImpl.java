package com.zhoubyte.scorpioelastic.service.impl;

import com.zhoubyte.scorpioelastic.dto.aggregation.AgeRangeAggregationRequest;
import com.zhoubyte.scorpioelastic.dto.aggregation.AgeRangeRequest;
import com.zhoubyte.scorpioelastic.dto.aggregation.AggregationBucketResponse;
import com.zhoubyte.scorpioelastic.dto.aggregation.FieldTermsAggregationRequest;
import com.zhoubyte.scorpioelastic.entity.DynamicDocument;
import com.zhoubyte.scorpioelastic.entity.UserDocEntity;
import com.zhoubyte.scorpioelastic.service.AggregationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AggregationServiceImpl implements AggregationService {

    private static final int MAX_AGGREGATION_SOURCE_SIZE = 10000;

    private final ElasticsearchTemplate elasticsearchTemplate;

    public AggregationServiceImpl(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public List<AggregationBucketResponse> aggregateUserAgeRanges(AgeRangeAggregationRequest request) {
        List<AgeRangeRequest> ranges = request.ranges() == null || request.ranges().isEmpty()
                ? defaultAgeRanges()
                : request.ranges();
        List<UserDocEntity> users = searchAllUsers();
        return ranges.stream()
                .map(range -> new AggregationBucketResponse(range.name(), countAgeRange(users, range)))
                .toList();
    }

    @Override
    public List<AggregationBucketResponse> aggregateDynamicTerms(FieldTermsAggregationRequest request) {
        NativeQuery query = matchAllQuery();
        SearchHits<DynamicDocument> searchHits = elasticsearchTemplate.search(
                query,
                DynamicDocument.class,
                IndexCoordinates.of(request.indexName())
        );
        return searchHits.stream()
                .map(SearchHit::getContent)
                .map(document -> document.get(request.fieldName()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(String::valueOf, LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(request.actualSize())
                .map(entry -> new AggregationBucketResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<UserDocEntity> searchAllUsers() {
        SearchHits<UserDocEntity> searchHits = elasticsearchTemplate.search(matchAllQuery(), UserDocEntity.class);
        return searchHits.stream().map(SearchHit::getContent).toList();
    }

    private NativeQuery matchAllQuery() {
        return NativeQuery.builder()
                .withQuery(query -> query.matchAll(matchAll -> matchAll))
                .withPageable(PageRequest.of(0, MAX_AGGREGATION_SOURCE_SIZE))
                .build();
    }

    private List<AgeRangeRequest> defaultAgeRanges() {
        return List.of(
                new AgeRangeRequest("0-18", 0, 18),
                new AgeRangeRequest("19-35", 19, 35),
                new AgeRangeRequest("36-60", 36, 60),
                new AgeRangeRequest("60+", 61, Integer.MAX_VALUE)
        );
    }

    private long countAgeRange(List<UserDocEntity> users, AgeRangeRequest range) {
        return users.stream()
                .filter(user -> user.getAge() != null)
                .filter(user -> user.getAge() >= range.min() && user.getAge() <= range.max())
                .count();
    }
}
