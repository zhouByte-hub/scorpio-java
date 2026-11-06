package com.zhoubyte.doclearning.word.controller;

import com.zhoubyte.doclearning.common.OutputFiles;
import com.zhoubyte.doclearning.word.service.WordDemoService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Word 学习示例接口。
 *
 * <p>使用方式：</p>
 * <ol>
 *   <li>生成演示文件：GET http://localhost:8090/demo/word/create</li>
 *   <li>解析上传文件：POST http://localhost:8090/demo/word/read ，form-data 参数名 file</li>
 * </ol>
 */
@RestController
@RequestMapping("/demo/word")
public class WordDemoController {

    private final WordDemoService wordDemoService;

    public WordDemoController(WordDemoService wordDemoService) {
        this.wordDemoService = wordDemoService;
    }

    /** 生成包含标题/富文本/表格/图片/页眉的演示 docx。 */
    @GetMapping("/create")
    public ResponseEntity<Resource> create() throws Exception {
        Path target = OutputFiles.resolve("demo-created.docx");
        try (var output = Files.newOutputStream(target)) {
            wordDemoService.createDemoDocument(output);
        }
        Resource resource = new UrlResource(target.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=demo-created.docx")
                .body(resource);
    }

    /** 上传 docx 并按文档顺序解析段落/表格/页眉。 */
    @PostMapping("/read")
    public Map<String, Object> read(@RequestParam("file") MultipartFile file) throws Exception {
        List<String> lines = wordDemoService.readDocument(file.getBytes());
        return Map.of(
                "fileName", file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                "elements", lines
        );
    }
}
