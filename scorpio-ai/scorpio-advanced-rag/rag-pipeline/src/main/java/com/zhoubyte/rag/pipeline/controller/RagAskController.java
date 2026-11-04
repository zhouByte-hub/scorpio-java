package com.zhoubyte.rag.pipeline.controller;

import com.zhoubyte.rag.pipeline.model.RagAnswer;
import com.zhoubyte.rag.pipeline.service.RagPipelineService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问答入口：执行完整 RAG 流水线，返回带引用来源的答案与全链路轨迹。
 */
@RestController
@RequestMapping("/ask")
public class RagAskController {

    private final RagPipelineService ragPipelineService;

    public RagAskController(RagPipelineService ragPipelineService) {
        this.ragPipelineService = ragPipelineService;
    }

    /**
     * 提问示例：
     * <pre>curl -X POST http://localhost:8100/advanced-rag/ask -H 'Content-Type: application/json' \
     *   -d '{"query":"差旅住宿费报销标准是多少"}'</pre>
     */
    public record AskRequest(String query) {
    }

    @PostMapping
    public RagAnswer ask(@RequestBody AskRequest request) {
        return ragPipelineService.ask(request.query());
    }
}
