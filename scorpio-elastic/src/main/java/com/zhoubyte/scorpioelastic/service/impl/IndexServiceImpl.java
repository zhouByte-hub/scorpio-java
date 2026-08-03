package com.zhoubyte.scorpioelastic.service.impl;

import com.zhoubyte.scorpioelastic.service.IndexService;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

@Service
public class IndexServiceImpl implements IndexService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    public IndexServiceImpl(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }
}
