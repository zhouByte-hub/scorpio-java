package com.zhoubyte.doclearning.pdf.controller;

import com.zhoubyte.doclearning.common.OutputFiles;
import com.zhoubyte.doclearning.pdf.service.PdfDemoService;
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

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PDFBox 学习示例接口。
 *
 * <p>使用方式（先用 /create 生成 demo.pdf，再把它上传给其他接口体验）：</p>
 * <ol>
 *   <li>生成演示文件：GET http://localhost:8090/demo/pdf/create</li>
 *   <li>文本提取：POST /demo/pdf/extract-text ，参数 file（可选 startPage/endPage）</li>
 *   <li>页面渲染成图片：POST /demo/pdf/render ，参数 file、page（0 基）、dpi</li>
 *   <li>合并：POST /demo/pdf/merge ，参数 file（复制两份合并演示）</li>
 *   <li>拆分：POST /demo/pdf/split ，参数 file，返回每页文件名列表</li>
 *   <li>加密：POST /demo/pdf/encrypt ，参数 file、ownerPassword、userPassword</li>
 *   <li>解密提取：POST /demo/pdf/encrypted-text ，参数 file、password</li>
 * </ol>
 */
@RestController
@RequestMapping("/demo/pdf")
public class PdfDemoController {

    private final PdfDemoService pdfDemoService;

    public PdfDemoController(PdfDemoService pdfDemoService) {
        this.pdfDemoService = pdfDemoService;
    }

    /** 生成两页演示 PDF。 */
    @GetMapping("/create")
    public ResponseEntity<Resource> create() throws Exception {
        Path target = OutputFiles.resolve("demo-created.pdf");
        try (var output = Files.newOutputStream(target)) {
            pdfDemoService.createDemoPdf(output);
        }
        return download(target, "demo-created.pdf", MediaType.APPLICATION_PDF);
    }

    /** 上传 PDF，提取文本（支持页码范围）。 */
    @PostMapping("/extract-text")
    public Map<String, Object> extractText(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "startPage", required = false) Integer startPage,
                                           @RequestParam(value = "endPage", required = false) Integer endPage) throws Exception {
        String text = pdfDemoService.extractText(file.getBytes(), startPage, endPage);
        return Map.of("text", text);
    }

    /** 上传 PDF，把指定页渲染为 PNG 下载。 */
    @PostMapping("/render")
    public ResponseEntity<Resource> render(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "dpi", defaultValue = "150") float dpi) throws Exception {
        BufferedImage image = pdfDemoService.renderPageToImage(file.getBytes(), page, dpi);
        Path target = OutputFiles.resolve("demo-rendered-page-" + (page + 1) + ".png");
        try (var output = Files.newOutputStream(target)) {
            pdfDemoService.writePng(image, output);
        }
        return download(target, target.getFileName().toString(), MediaType.IMAGE_PNG);
    }

    /** 上传 PDF，合并（演示：复制两份合并）后下载。 */
    @PostMapping("/merge")
    public ResponseEntity<Resource> merge(@RequestParam("file") MultipartFile file) throws Exception {
        Path target = OutputFiles.resolve("demo-merged.pdf");
        try (var output = Files.newOutputStream(target)) {
            pdfDemoService.mergePdf(file.getBytes(), output);
        }
        return download(target, "demo-merged.pdf", MediaType.APPLICATION_PDF);
    }

    /** 上传 PDF，按页拆分，返回生成的单页文件名列表。 */
    @PostMapping("/split")
    public Map<String, Object> split(@RequestParam("file") MultipartFile file) throws Exception {
        List<byte[]> pages = pdfDemoService.splitPdf(file.getBytes());
        List<String> fileNames = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            Path target = OutputFiles.resolve("demo-split-page-" + (i + 1) + ".pdf");
            Files.write(target, pages.get(i));
            fileNames.add(target.toString());
        }
        return Map.of("pages", fileNames);
    }

    /** 上传 PDF，加密后下载（禁止打印与复制文本）。 */
    @PostMapping("/encrypt")
    public ResponseEntity<Resource> encrypt(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "ownerPassword", defaultValue = "owner123") String ownerPassword,
                                            @RequestParam(value = "userPassword", defaultValue = "user123") String userPassword) throws Exception {
        byte[] encrypted = pdfDemoService.encryptPdf(file.getBytes(), ownerPassword, userPassword);
        Path target = OutputFiles.resolve("demo-encrypted.pdf");
        Files.write(target, encrypted);
        return download(target, "demo-encrypted.pdf", MediaType.APPLICATION_PDF);
    }

    /** 上传加密 PDF，用密码打开并提取文本。 */
    @PostMapping("/encrypted-text")
    public Map<String, Object> encryptedText(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "password", defaultValue = "user123") String password) throws Exception {
        String text = pdfDemoService.extractTextFromEncrypted(file.getBytes(), password);
        return Map.of("text", text);
    }

    /** 公共下载响应封装。 */
    private ResponseEntity<Resource> download(Path target, String fileName, MediaType type) throws Exception {
        Resource resource = new UrlResource(target.toUri());
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(resource);
    }
}
