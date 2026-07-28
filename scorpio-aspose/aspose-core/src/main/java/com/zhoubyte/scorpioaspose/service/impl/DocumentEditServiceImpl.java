package com.zhoubyte.scorpioaspose.service.impl;

import com.aspose.words.Document;
import com.aspose.words.HtmlSaveOptions;
import com.aspose.words.LoadFormat;
import com.aspose.words.LoadOptions;
import com.aspose.words.SaveFormat;
import com.zhoubyte.scorpioaspose.dto.DocumentDto;
import com.zhoubyte.scorpioaspose.service.DocumentEditService;
import com.zhoubyte.scorpioaspose.service.DocumentStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentEditServiceImpl implements DocumentEditService {

    private static final Logger log = LoggerFactory.getLogger(DocumentEditServiceImpl.class);
    private static final Pattern BODY_PATTERN = Pattern.compile(
            "(?is)<body[^>]*>(.*)</body>");

    private final DocumentStorageService documentStorageService;

    @Value("${server.servlet.context-path:/aspose}")
    private String contextPath;

    public DocumentEditServiceImpl(DocumentStorageService documentStorageService) {
        this.documentStorageService = documentStorageService;
    }

    @Override
    public String toHtml(String fileId) {
        File wordFile = requireWordFile(fileId);
        try {
            Document document = new Document(wordFile.getAbsolutePath());
            HtmlSaveOptions options = new HtmlSaveOptions(SaveFormat.HTML);
            options.setExportImagesAsBase64(true);
            options.setPrettyFormat(true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream, options);
            String fullHtml = outputStream.toString(StandardCharsets.UTF_8);
            return extractBody(fullHtml);
        } catch (Exception e) {
            throw new RuntimeException("文档转 HTML 失败：" + e.getMessage(), e);
        }
    }

    @Override
    public DocumentDto saveHtml(String fileId, String html) {
        if (html == null || html.isBlank()) {
            throw new RuntimeException("HTML 内容不能为空");
        }
        File wordFile = requireWordFile(fileId);
        String wrappedHtml = wrapHtml(html);
        try {
            LoadOptions loadOptions = new LoadOptions();
            loadOptions.setLoadFormat(LoadFormat.HTML);
            Document document = new Document(
                    new ByteArrayInputStream(wrappedHtml.getBytes(StandardCharsets.UTF_8)),
                    loadOptions);

            File targetFile = resolveSaveTarget(wordFile);
            document.save(targetFile.getAbsolutePath(), SaveFormat.DOCX);

            if (!targetFile.equals(wordFile) && wordFile.exists()) {
                boolean deleted = wordFile.delete();
                if (!deleted) {
                    log.warn("旧格式文件删除失败：{}", wordFile.getAbsolutePath());
                }
            }
            log.info("文档保存成功：{}", targetFile.getAbsolutePath());
            return DocumentDto.fromFile(targetFile, contextPath);
        } catch (Exception e) {
            throw new RuntimeException("HTML 保存为 Word 失败：" + e.getMessage(), e);
        }
    }

    private File requireWordFile(String fileId) {
        File file = documentStorageService.findFileById(fileId);
        if (file == null || !file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".doc") && !name.endsWith(".docx")) {
            throw new RuntimeException("仅支持 Word 文档编辑");
        }
        return file;
    }

    private File resolveSaveTarget(File original) {
        String name = original.getName();
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".docx")) {
            return original;
        }
        if (lower.endsWith(".doc")) {
            String newName = name.substring(0, name.length() - 4) + ".docx";
            return new File(original.getParentFile(), newName);
        }
        return new File(original.getParentFile(), name + ".docx");
    }

    private String extractBody(String fullHtml) {
        Matcher matcher = BODY_PATTERN.matcher(fullHtml);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return fullHtml;
    }

    private String wrapHtml(String bodyHtml) {
        String trimmed = bodyHtml.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("<html") || lower.startsWith("<!doctype")) {
            return trimmed;
        }
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/></head><body>"
                + trimmed
                + "</body></html>";
    }
}
