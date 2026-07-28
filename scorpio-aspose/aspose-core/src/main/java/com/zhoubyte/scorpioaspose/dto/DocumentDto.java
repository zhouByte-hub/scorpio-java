package com.zhoubyte.scorpioaspose.dto;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 文档信息DTO
 */
public class DocumentDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private String id;
    private String name;
    private String type;
    private String fileName;
    private Long fileSize;
    private String fileUrl;
    private String createdAt;
    private String updatedAt;

    public static DocumentDto fromFile(File file, String contextPath) {
        DocumentDto dto = new DocumentDto();
        String fileName = file.getName();
        File parent = file.getParentFile();
        // 优先用父目录名作为 ID（上传约定：{type}/{uuid}/{originalName.ext}）
        String id = parent != null && !isTypeDir(parent.getName())
                ? parent.getName()
                : (fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName);

        dto.setId(id);
        dto.setFileName(fileName);
        dto.setName(fileName);
        dto.setFileSize(file.length());

        String ext = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        dto.setType("pdf".equals(ext) ? "pdf" : "word");
        dto.setFileUrl(contextPath + "/documents/" + id + "/download");

        String time = FORMATTER.format(Instant.ofEpochMilli(file.lastModified()));
        dto.setCreatedAt(time);
        dto.setUpdatedAt(time);
        return dto;
    }

    private static boolean isTypeDir(String name) {
        return "docx".equalsIgnoreCase(name)
                || "doc".equalsIgnoreCase(name)
                || "pdf".equalsIgnoreCase(name)
                || "word".equalsIgnoreCase(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
