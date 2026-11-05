package com.zhoubyte.rag.ingestion.controller;

import com.zhoubyte.rag.ingestion.model.IngestChunk;
import com.zhoubyte.rag.ingestion.parser.ParserRouter;
import com.zhoubyte.rag.ingestion.service.IngestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文档导入接口：文件上传入库、分块预览、chunk 查询。
 */
@RestController
@RequestMapping("/documents")
public class IngestionController {

    private final IngestionService ingestionService;
    private final ParserRouter parserRouter;

    public IngestionController(IngestionService ingestionService, ParserRouter parserRouter) {
        this.ingestionService = ingestionService;
        this.parserRouter = parserRouter;
    }

    /**
     * 上传文件并入库。
     */
    @PostMapping
    public IngestionService.ImportResult upload(@RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "title", required = false) String title)
            throws IOException {
        requireFile(file);
        return ingestionService.ingest(file.getBytes(), file.getOriginalFilename(), title);
    }

    /**
     * 只解析与分块，不入库——用于观察不同格式/参数下的切分效果。
     */
    @PostMapping("/preview")
    public IngestionService.PreviewResult preview(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(value = "title", required = false) String title)
            throws IOException {
        requireFile(file);
        return ingestionService.preview(file.getBytes(), file.getOriginalFilename(), title);
    }

    /**
     * 查看某文档的全部 chunk。
     */
    @GetMapping("/{docId}/chunks")
    public List<IngestChunk> chunks(@PathVariable("docId") String docId) {
        return ingestionService.findByDocId(docId);
    }

    /**
     * 当前支持的文件类型。
     */
    @GetMapping("/supported-types")
    public List<String> supportedTypes() {
        return parserRouter.supportedExtensions();
    }

    private void requireFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("请上传非空文件");
        }
    }
}
