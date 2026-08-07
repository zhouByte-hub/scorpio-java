package com.zhoubyte.scorpioelastic.service;

import com.zhoubyte.scorpioelastic.dto.index.CreateIndexRequest;
import com.zhoubyte.scorpioelastic.dto.index.IndexFieldRequest;
import com.zhoubyte.scorpioelastic.dto.index.IndexInfoResponse;

import java.util.List;
import java.util.Map;

public interface IndexService {

    Boolean createIndex(CreateIndexRequest request);

    Boolean deleteIndex(String indexName);

    Boolean exists(String indexName);

    IndexInfoResponse getIndexInfo(String indexName);

    Map<String, Object> putMapping(String indexName, List<IndexFieldRequest> fields);

    List<String> listIndices();
}
