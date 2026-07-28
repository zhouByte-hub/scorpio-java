package com.zhoubyte.scorpioaspose.controller;

import com.zhoubyte.scorpioaspose.dto.DocumentDto;
import com.zhoubyte.scorpioaspose.service.DocumentStorageService;
import com.zhoubyte.scorpioaspose.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class UploadController<T> {

    @Autowired
    protected T service;

    @Autowired
    private DocumentStorageService documentStorageService;

    @Value("${server.servlet.context-path:/aspose}")
    private String contextPath;

    private final Set<String> allowedTypes;
    private final String primaryType;

    /**
     * @param types 允许的后缀，逗号分隔，如 "DOCX" 或 "DOCX,DOC"
     */
    protected UploadController(String types) {
        this.allowedTypes = Arrays.stream(types.split(","))
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        this.primaryType = this.allowedTypes.iterator().next();
    }

    @PostMapping(value = "/upload")
    public Result<DocumentDto> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "文件名不能为空");
        String[] split = originalFilename.split("\\.");
        if (split.length < 2) {
            throw new RuntimeException("文件缺少后缀，支持 Word / PDF");
        }
        String suffix = split[split.length - 1];
        String suffixUpper = suffix.toUpperCase(Locale.ROOT);
        if (!allowedTypes.contains(suffixUpper)) {
            throw new RuntimeException("格式错误，所需格式：" + String.join(",", allowedTypes));
        }

        // Word 统一落到 docx 目录；PDF 落到 pdf 目录
        String typeDirName = "PDF".equals(suffixUpper) ? "pdf" : "docx";
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
            throw new RuntimeException("文件上传失败", e);
        }

        return Result.success(DocumentDto.fromFile(targetFile, contextPath));
    }

    private String sanitizeFileName(String fileName) {
        String name = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isBlank()) {
            return primaryType.toLowerCase(Locale.ROOT) + "." + primaryType.toLowerCase(Locale.ROOT);
        }
        return name;
    }
}
