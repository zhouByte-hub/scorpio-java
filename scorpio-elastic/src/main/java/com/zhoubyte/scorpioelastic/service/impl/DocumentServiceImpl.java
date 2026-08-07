package com.zhoubyte.scorpioelastic.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.zhoubyte.scorpioelastic.dto.document.BatchDeleteRequest;
import com.zhoubyte.scorpioelastic.dto.document.BatchUserDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.DynamicDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.DynamicSearchRequest;
import com.zhoubyte.scorpioelastic.dto.document.PageResponse;
import com.zhoubyte.scorpioelastic.dto.document.UserDocumentRequest;
import com.zhoubyte.scorpioelastic.dto.document.UserHighlightResponse;
import com.zhoubyte.scorpioelastic.dto.document.UserSearchRequest;
import com.zhoubyte.scorpioelastic.entity.DynamicDocument;
import com.zhoubyte.scorpioelastic.entity.UserDocEntity;
import com.zhoubyte.scorpioelastic.exception.ElasticBusinessException;
import com.zhoubyte.scorpioelastic.service.DocumentService;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    public DocumentServiceImpl(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public UserDocEntity saveUser(UserDocumentRequest request) {
        return elasticsearchTemplate.save(toEntity(request));
    }

    @Override
    public UserDocEntity getUser(String id) {
        UserDocEntity entity = elasticsearchTemplate.get(id, UserDocEntity.class);
        if (entity == null) {
            throw new ElasticBusinessException("用户文档不存在：" + id);
        }
        return entity;
    }

    @Override
    public UserDocEntity updateUser(String id, UserDocumentRequest request) {
        getUser(id);
        UserDocEntity entity = toEntity(request);
        entity.setId(id);
        return elasticsearchTemplate.save(entity);
    }

    @Override
    public Boolean deleteUser(String id) {
        getUser(id);
        elasticsearchTemplate.delete(id, UserDocEntity.class);
        return true;
    }

    @Override
    public List<UserDocEntity> batchSaveUsers(BatchUserDocumentRequest request) {
        List<UserDocEntity> savedUsers = new ArrayList<>();
        for (UserDocumentRequest document : request.documents()) {
            savedUsers.add(saveUser(document));
        }
        return savedUsers;
    }

    @Override
    public Boolean batchDeleteUsers(BatchDeleteRequest request) {
        for (String id : request.ids()) {
            if (StringUtils.hasText(id)) {
                elasticsearchTemplate.delete(id, UserDocEntity.class);
            }
        }
        return true;
    }

    @Override
    public PageResponse<UserDocEntity> searchUsers(UserSearchRequest request) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(userKeywordQuery(request.keyword()))
                .withPageable(userPageable(request))
                .build();
        SearchHits<UserDocEntity> searchHits = elasticsearchTemplate.search(query, UserDocEntity.class);
        List<UserDocEntity> content = searchHits.stream().map(SearchHit::getContent).toList();
        return new PageResponse<>(searchHits.getTotalHits(), request.actualPage(), request.actualSize(), content);
    }

    @Override
    public PageResponse<UserHighlightResponse> highlightSearchUsers(UserSearchRequest request) {
        PageResponse<UserDocEntity> page = searchUsers(request);
        List<UserHighlightResponse> content = page.content().stream()
                .map(user -> toHighlightResponse(user, request.keyword()))
                .toList();
        return new PageResponse<>(page.total(), page.page(), page.size(), content);
    }

    @Override
    public Map<String, Object> saveDynamicDocument(DynamicDocumentRequest request) {
        IndexQueryBuilder builder = new IndexQueryBuilder().withObject(request.content());
        if (StringUtils.hasText(request.id())) {
            builder.withId(request.id());
        }
        IndexQuery indexQuery = builder.build();
        String documentId = elasticsearchTemplate.index(indexQuery, IndexCoordinates.of(request.indexName()));
        return getDynamicDocument(request.indexName(), documentId);
    }

    @Override
    public Map<String, Object> getDynamicDocument(String indexName, String id) {
        DynamicDocument document = elasticsearchTemplate.get(id, DynamicDocument.class, IndexCoordinates.of(indexName));
        if (document == null) {
            throw new ElasticBusinessException("动态文档不存在：" + id);
        }
        Map<String, Object> response = new LinkedHashMap<>(document);
        response.put("_id", id);
        return response;
    }

    @Override
    public Boolean deleteDynamicDocument(String indexName, String id) {
        elasticsearchTemplate.delete(id, IndexCoordinates.of(indexName));
        return true;
    }

    @Override
    public PageResponse<Map<String, Object>> searchDynamicDocuments(DynamicSearchRequest request) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(queryStringQuery(request.keyword()))
                .withPageable(PageRequest.of(request.actualPage(), request.actualSize()))
                .build();
        SearchHits<DynamicDocument> searchHits = elasticsearchTemplate.search(
                query,
                DynamicDocument.class,
                IndexCoordinates.of(request.indexName())
        );
        List<Map<String, Object>> content = searchHits.stream()
                .map(hit -> withDocumentId(hit.getId(), hit.getContent()))
                .toList();
        return new PageResponse<>(searchHits.getTotalHits(), request.actualPage(), request.actualSize(), content);
    }

    private UserDocEntity toEntity(UserDocumentRequest request) {
        UserDocEntity entity = new UserDocEntity();
        entity.setId(request.id());
        entity.setUsername(request.username());
        entity.setEmail(request.email());
        entity.setAge(request.age());
        return entity;
    }

    private Pageable userPageable(UserSearchRequest request) {
        Sort.Direction direction = "asc".equalsIgnoreCase(request.actualSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(request.actualPage(), request.actualSize(), Sort.by(direction, request.actualSortField()));
    }

    private Query userKeywordQuery(String keyword) {
        return Query.of(query -> query.multiMatch(multiMatch -> multiMatch
                .query(keyword)
                .fields("username", "email")));
    }

    private Query queryStringQuery(String keyword) {
        return Query.of(query -> query.queryString(queryString -> queryString.query(keyword)));
    }

    private UserHighlightResponse toHighlightResponse(UserDocEntity entity, String keyword) {
        Map<String, List<String>> highlightFields = new LinkedHashMap<>();
        highlightFields.put("username", highlight(entity.getUsername(), keyword));
        highlightFields.put("email", highlight(entity.getEmail(), keyword));
        return new UserHighlightResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getAge(),
                highlightFields
        );
    }

    private List<String> highlight(String value, String keyword) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(keyword) || !value.contains(keyword)) {
            return List.of();
        }
        return List.of(value.replace(keyword, "<em>" + keyword + "</em>"));
    }

    private Map<String, Object> withDocumentId(String id, DynamicDocument document) {
        Map<String, Object> response = new LinkedHashMap<>(document);
        response.put("_id", id);
        return response;
    }
}
