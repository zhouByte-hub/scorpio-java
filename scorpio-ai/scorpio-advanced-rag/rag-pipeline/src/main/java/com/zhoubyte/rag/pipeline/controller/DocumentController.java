package com.zhoubyte.rag.pipeline.controller;

import com.zhoubyte.rag.pipeline.model.DocumentImportRequest;
import com.zhoubyte.rag.pipeline.model.MetadataInfo;
import com.zhoubyte.rag.pipeline.service.DocumentIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档导入入口：支持单篇导入与批量导入内置示例文档。
 */
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentIngestService ingestService;

    public DocumentController(DocumentIngestService ingestService) {
        this.ingestService = ingestService;
    }

    /**
     * 导入单篇文档：
     * <pre>curl -X POST http://localhost:8100/advanced-rag/documents -H 'Content-Type: application/json' -d '{
     *   "title":"XX制度","content":"正文……",
     *   "metadata":{"department":"财务部","level":"public","docType":"规章制度","docDate":"2026-01-01"}
     * }'</pre>
     */
    @PostMapping
    public Map<String, Object> ingest(@RequestBody DocumentImportRequest request) {
        int chunks = ingestService.ingest(request);
        return Map.of("chunks", chunks);
    }

    /**
     * 批量导入 classpath:samples/ 下的示例文档（带 front-matter 元数据）。
     */
    @PostMapping("/import-samples")
    public List<Map<String, Object>> importSamples() throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:samples/*.md");
        for (Resource resource : resources) {
            String text = resource.getContentAsString(StandardCharsets.UTF_8);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("file", resource.getFilename());
            try {
                result.put("chunks", ingestService.ingest(parseSample(text)));
            } catch (Exception e) {
                log.warn("示例文档导入失败: {}", resource.getFilename(), e);
                result.put("error", e.getMessage());
            }
            results.add(result);
        }
        return results;
    }

    /**
     * 解析 front-matter 格式（--- 包裹的 key: value 元数据块）的示例文档。
     */
    private DocumentImportRequest parseSample(String text) {
        if (!text.startsWith("---")) {
            throw new IllegalArgumentException("示例文档缺少 front-matter 元数据");
        }
        int end = text.indexOf("---", 3);
        if (end < 0) {
            throw new IllegalArgumentException("front-matter 未闭合");
        }
        Map<String, String> meta = new LinkedHashMap<>();
        for (String line : text.substring(3, end).split("\n")) {
            int sep = line.indexOf(':');
            if (sep > 0) {
                meta.put(line.substring(0, sep).trim(), line.substring(sep + 1).trim());
            }
        }
        MetadataInfo metadata = new MetadataInfo(
                meta.get("department"), meta.get("level"), meta.get("docType"), meta.get("docDate"));
        return new DocumentImportRequest(meta.getOrDefault("title", "未命名"),
                text.substring(end + 3).trim(), metadata, null, null);
    }
}
