package com.zhoubyte.core.service;

import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.util.List;

public interface RagService {

    void importData(String data);

    List<Document> query(String message);

    Flux<String> retrievalQuery(String message);
}
