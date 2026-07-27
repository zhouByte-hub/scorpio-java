package com.zhoubyte.scorpioaspose.dto;

import java.io.File;

/**
 * 文档信息DTO
 */
public class DocumentDto {

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
        // 从文件名提取ID（文件名格式：{uuid}.{ext}）
        String id = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        dto.setId(id);
        dto.setFileName(fileName);
        dto.setName(fileName);
        dto.setFileSize(file.length());
        // 根据扩展名判断类型
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "";
        if ("pdf".equals(ext)) {
            dto.setType("pdf");
        } else {
            dto.setType("word");
        }
        // 构建下载URL
        String parentDirName = file.getParentFile() != null ? file.getParentFile().getName() : "";
        dto.setFileUrl(contextPath + "/documents/" + parentDirName + "/download");
        dto.setCreatedAt(String.valueOf(file.lastModified()));
        dto.setUpdatedAt(String.valueOf(file.lastModified()));
        return dto;
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
