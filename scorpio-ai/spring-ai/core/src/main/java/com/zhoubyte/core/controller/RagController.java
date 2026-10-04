package com.zhoubyte.core.controller;

import com.zhoubyte.core.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping(value = "/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping(value = "/import")
    public void importData(@RequestParam("message") String message) {
        ragService.importData(message);
    }

    @GetMapping(value = "/basic:query")
    public List<Document> basicQuery(@RequestParam("message") String message) {
        return ragService.query(message);
    }

    @GetMapping(value = "/retrieval:query",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> retrievalQuery(@RequestParam("message") String message) {
        return ragService.retrievalQuery(message);
    }
}
