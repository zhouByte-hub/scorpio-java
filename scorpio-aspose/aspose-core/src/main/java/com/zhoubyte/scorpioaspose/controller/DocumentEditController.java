package com.zhoubyte.scorpioaspose.controller;

import com.zhoubyte.scorpioaspose.dto.DocumentDto;
import com.zhoubyte.scorpioaspose.dto.DocumentHtmlDto;
import com.zhoubyte.scorpioaspose.service.DocumentEditService;
import com.zhoubyte.scorpioaspose.service.DocumentStorageService;
import com.zhoubyte.scorpioaspose.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 文档在线编辑与统一上传
 */
@RestController
@RequestMapping("/documents")
public class DocumentEditController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("doc", "docx", "pdf");

    private final DocumentEditService documentEditService;
    private final DocumentStorageService documentStorageService;

    @Value("${server.servlet.context-path:/aspose}")
    private String contextPath;

    public DocumentEditController(DocumentEditService documentEditService,
                                  DocumentStorageService documentStorageService) {
        this.documentEditService = documentEditService;
        this.documentStorageService = documentStorageService;
    }

    @GetMapping("/{fileId}/html")
    public Result<DocumentHtmlDto> getHtml(@PathVariable("fileId") String fileId) {
        try {
            String html = documentEditService.toHtml(fileId);
            return Result.success(new DocumentHtmlDto(fileId, html));
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/{fileId}/html")
    public Result<DocumentDto> saveHtml(@PathVariable("fileId") String fileId,
                                        @RequestBody DocumentHtmlDto body) {
        try {
            String html = body == null ? null : body.getHtml();
            DocumentDto dto = documentEditService.saveHtml(fileId, html);
            return Result.success(dto);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/upload")
    public Result<DocumentDto> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "文件名不能为空");
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0) {
            return Result.fail("文件缺少后缀，支持 Word / PDF");
        }
        String suffix = originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(suffix)) {
            return Result.fail("格式错误，支持：doc、docx、pdf");
        }

        String typeDirName = "pdf".equals(suffix) ? "pdf" : "docx";
        String uploadPath = documentStorageService.getTypeDir(typeDirName);
        String dir = UUID.randomUUID().toString().replace("-", "");
        String safeName = sanitizeFileName(originalFilename);
        File targetFile = new File(uploadPath + "/" + dir + "/" + safeName);

        try {
            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists()) {
                Files.createDirectories(parentDir.toPath());
            }
            file.transferTo(targetFile);
        } catch (IOException e) {
            return Result.fail("文件上传失败");
        }

        return Result.success(DocumentDto.fromFile(targetFile, contextPath));
    }

    private String sanitizeFileName(String fileName) {
        String name = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isBlank()) {
            return "document.bin";
        }
        return name;
    }
}
