package com.zhoubyte.doclearning.excel.controller;

import com.zhoubyte.doclearning.common.OutputFiles;
import com.zhoubyte.doclearning.excel.service.ExcelDemoService;
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
import java.util.Map;

/**
 * Excel 学习示例接口。
 *
 * <p>使用方式：</p>
 * <ol>
 *   <li>生成演示文件：GET http://localhost:8090/demo/excel/create （浏览器直接访问即可下载）</li>
 *   <li>解析上传文件：POST http://localhost:8090/demo/excel/read ，form-data 参数名 file</li>
 * </ol>
 */
@RestController
@RequestMapping("/demo/excel")
public class ExcelDemoController {

    private final ExcelDemoService excelDemoService;

    public ExcelDemoController(ExcelDemoService excelDemoService) {
        this.excelDemoService = excelDemoService;
    }

    /**
     * 生成一个包含样式/公式/合并单元格/下拉框/图片的演示 Excel。
     * 生成后同时落盘到 target/output（方便对照源码查看），并把文件内容作为下载响应返回。
     */
    @GetMapping("/create")
    public ResponseEntity<Resource> create() throws Exception {
        Path target = OutputFiles.resolve("demo-created.xlsx");
        try (var output = Files.newOutputStream(target)) {
            excelDemoService.createDemoWorkbook(output);
        }
        // UrlResource：把本地文件包装成可流式下载的 Resource
        Resource resource = new UrlResource(target.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=demo-created.xlsx")
                .body(resource);
    }

    /**
     * 上传 Excel 并解析其内容（含公式计算结果），以 JSON 返回。
     * curl 示例：
     * curl -F "file=@demo-created.xlsx" http://localhost:8090/demo/excel/read
     */
    @PostMapping("/read")
    public Map<String, Object> read(@RequestParam("file") MultipartFile file) throws Exception {
        // MultipartFile#getBytes 一次性读入内存，适合演示；
        // 大文件应使用 getInputStream() 流式处理
        var parsed = excelDemoService.readWorkbook(file.getBytes());
        return Map.of(
                "fileName", file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                "sheets", parsed
        );
    }
}
