package com.zhoubyte.scorpioelastic.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import com.zhoubyte.scorpioelastic.dto.index.CreateIndexRequest;
import com.zhoubyte.scorpioelastic.dto.index.IndexFieldRequest;
import com.zhoubyte.scorpioelastic.dto.index.IndexInfoResponse;
import com.zhoubyte.scorpioelastic.exception.ElasticBusinessException;
import com.zhoubyte.scorpioelastic.service.IndexService;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IndexServiceImpl implements IndexService {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ElasticsearchClient elasticsearchClient;

    public IndexServiceImpl(ElasticsearchTemplate elasticsearchTemplate, ElasticsearchClient elasticsearchClient) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public Boolean createIndex(CreateIndexRequest request) {
        IndexOperations indexOperations = indexOperations(request.indexName());
        if (indexOperations.exists()) {
            throw new ElasticBusinessException("索引已存在：" + request.indexName());
        }

        boolean created = indexOperations.create();
        if (created) {
            indexOperations.putMapping(buildMapping(request.fields()));
        }
        return created;
    }

    @Override
    public Boolean deleteIndex(String indexName) {
        IndexOperations indexOperations = indexOperations(indexName);
        if (!indexOperations.exists()) {
            throw new ElasticBusinessException("索引不存在：" + indexName);
        }
        return indexOperations.delete();
    }

    @Override
    public Boolean exists(String indexName) {
        return indexOperations(indexName).exists();
    }

    @Override
    public IndexInfoResponse getIndexInfo(String indexName) {
        IndexOperations indexOperations = indexOperations(indexName);
        boolean exists = indexOperations.exists();
        Map<String, Object> mapping = exists ? indexOperations.getMapping() : Map.of();
        return new IndexInfoResponse(indexName, exists, mapping);
    }

    @Override
    public Map<String, Object> putMapping(String indexName, List<IndexFieldRequest> fields) {
        IndexOperations indexOperations = indexOperations(indexName);
        if (!indexOperations.exists()) {
            throw new ElasticBusinessException("索引不存在：" + indexName);
        }
        indexOperations.putMapping(buildMapping(fields));
        return indexOperations.getMapping();
    }

    @Override
    public List<String> listIndices() {
        try {
            GetIndexResponse response = elasticsearchClient.indices().get(request -> request.index("*"));
            return response.indices().keySet().stream().sorted().toList();
        } catch (IOException exception) {
            throw new ElasticBusinessException("获取索引列表失败", exception);
        }
    }

    private IndexOperations indexOperations(String indexName) {
        return elasticsearchTemplate.indexOps(IndexCoordinates.of(indexName));
    }

    private Document buildMapping(List<IndexFieldRequest> fields) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (IndexFieldRequest field : fields) {
            properties.put(field.name(), buildFieldMapping(field));
        }
        return Document.create().append("properties", properties);
    }

    private Map<String, Object> buildFieldMapping(IndexFieldRequest field) {
        Map<String, Object> fieldMapping = new LinkedHashMap<>();
        fieldMapping.put("type", field.type());
        if (StringUtils.hasText(field.analyzer())) {
            fieldMapping.put("analyzer", field.analyzer());
        }
        if (StringUtils.hasText(field.searchAnalyzer())) {
            fieldMapping.put("search_analyzer", field.searchAnalyzer());
        }
        return fieldMapping;
    }
}
