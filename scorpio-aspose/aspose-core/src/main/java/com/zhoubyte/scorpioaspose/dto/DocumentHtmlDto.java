package com.zhoubyte.scorpioaspose.dto;

/**
 * 文档 HTML 内容
 */
public class DocumentHtmlDto {

    private String fileId;
    private String html;

    public DocumentHtmlDto() {
    }

    public DocumentHtmlDto(String fileId, String html) {
        this.fileId = fileId;
        this.html = html;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
