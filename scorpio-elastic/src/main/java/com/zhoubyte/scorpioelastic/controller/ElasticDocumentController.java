package com.zhoubyte.scorpioelastic.controller;

import com.zhoubyte.scorpioelastic.service.DocumentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/doc")
public class ElasticDocumentController {

    private final DocumentService documentService;

    public ElasticDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }


}
