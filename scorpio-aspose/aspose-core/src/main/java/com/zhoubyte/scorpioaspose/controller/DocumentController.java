package com.zhoubyte.scorpioaspose.controller;

import com.zhoubyte.scorpioaspose.dto.DocumentDto;
import com.zhoubyte.scorpioaspose.service.DocumentStorageService;
import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 文档管理：列表、详情、下载、删除
 */
@RestController
@RequestMapping(value = "/documents")
public class DocumentController {

    private final DocumentStorageService documentStorageService;

    @Value("${server.servlet.context-path:/aspose}")
    private String contextPath;

    public DocumentController(DocumentStorageService documentStorageService) {
        this.documentStorageService = documentStorageService;
    }

    @GetMapping
    public Result<List<DocumentDto>> list() {
        List<DocumentDto> documents = new ArrayList<>();
        for (File file : documentStorageService.listDocumentFiles()) {
            documents.add(DocumentDto.fromFile(file, contextPath));
        }
        documents.sort(Comparator.comparing(DocumentDto::getUpdatedAt).reversed());
        return Result.success(documents);
    }

    @GetMapping(value = "/{fileId}")
    public Result<DocumentDto> detail(@PathVariable("fileId") String fileId) {
        File targetFile = documentStorageService.findFileById(fileId);
        if (targetFile == null || !targetFile.exists()) {
            return Result.fail("文件不存在");
        }
        return Result.success(DocumentDto.fromFile(targetFile, contextPath));
    }

    @GetMapping(value = "/{fileId}/download")
    public void download(@PathVariable("fileId") String fileId,
                         @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
                         HttpServletResponse response) {
        File targetFile = documentStorageService.findFileById(fileId);
        if (targetFile == null || !targetFile.exists()) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        String mode = "attachment".equalsIgnoreCase(disposition) ? "attachment" : "inline";
        response.setContentType(getContentType(targetFile.getName()));
        response.setHeader("Content-Disposition", mode + "; filename=\"" + encodeFileName(targetFile.getName()) + "\"");
        response.setContentLengthLong(targetFile.length());

        try (FileInputStream fis = new FileInputStream(targetFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @DeleteMapping(value = "/{fileId}")
    public Result<String> delete(@PathVariable("fileId") String fileId) {
        boolean deleted = documentStorageService.deleteById(fileId);
        if (!deleted) {
            return Result.error("文件不存在或删除失败");
        }
        return Result.success("删除成功");
    }

    private String encodeFileName(String fileName) {
        return fileName.replace("\"", "");
    }

    private String getContentType(String fileName) {
        String ext = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";
        };
    }
}
