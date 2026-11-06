package com.zhoubyte.doclearning.ppt.controller;

import com.zhoubyte.doclearning.common.OutputFiles;
import com.zhoubyte.doclearning.ppt.service.PptDemoService;
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
 * PPT 学习示例接口。
 *
 * <p>使用方式：</p>
 * <ol>
 *   <li>生成演示文件：GET http://localhost:8090/demo/ppt/create</li>
 *   <li>解析上传文件：POST http://localhost:8090/demo/ppt/read ，form-data 参数名 file</li>
 * </ol>
 */
@RestController
@RequestMapping("/demo/ppt")
public class PptDemoController {

    private final PptDemoService pptDemoService;

    public PptDemoController(PptDemoService pptDemoService) {
        this.pptDemoService = pptDemoService;
    }

    /** 生成包含标题页/列表页/图片页的演示 pptx。 */
    @GetMapping("/create")
    public ResponseEntity<Resource> create() throws Exception {
        Path target = OutputFiles.resolve("demo-created.pptx");
        try (var output = Files.newOutputStream(target)) {
            pptDemoService.createDemoPresentation(output);
        }
        Resource resource = new UrlResource(target.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=demo-created.pptx")
                .body(resource);
    }

    /** 上传 pptx 并逐页提取文本。 */
    @PostMapping("/read")
    public Map<String, Object> read(@RequestParam("file") MultipartFile file) throws Exception {
        List<String> content = pptDemoService.readPresentation(file.getBytes());
        return Map.of(
                "fileName", file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                "content", content
        );
    }
}
