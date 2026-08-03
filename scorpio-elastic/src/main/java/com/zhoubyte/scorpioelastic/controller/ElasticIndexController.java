package com.zhoubyte.scorpioelastic.controller;

import com.zhoubyte.scorpioelastic.service.IndexService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/index")
public class ElasticIndexController {

    private final IndexService indexService;

    public ElasticIndexController(IndexService indexService) {
        this.indexService = indexService;
    }
}
