package com.zhoubyte.scorpioelastic.service.impl;

import com.zhoubyte.scorpioelastic.service.DocumentService;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    public DocumentServiceImpl(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }
}
