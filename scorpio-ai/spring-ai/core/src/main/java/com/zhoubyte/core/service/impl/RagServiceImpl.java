package com.zhoubyte.core.service.impl;

import com.zhoubyte.core.service.RagService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RagServiceImpl implements RagService {

    private final VectorStore vectorStore;

    public RagServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void importData(String data) {
        if(StringUtils.isBlank(data)) {
            throw new RuntimeException("data is empty");
        }
        Document document = Document.builder()
                .id(UUID.randomUUID().toString())
                .score(10d)
                .text(data)
                .build();
        vectorStore.add(List.of(document));
    }

    @Override
    public List<Document> query(String message) {
        SearchRequest build = SearchRequest.builder()
                .similarityThreshold(8.0)
                .topK(2)
                .query(message)
                .build();
        return vectorStore.similaritySearch(build);
    }
}
