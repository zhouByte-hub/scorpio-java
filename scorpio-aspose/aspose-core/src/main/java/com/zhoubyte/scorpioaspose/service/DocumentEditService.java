package com.zhoubyte.scorpioaspose.service;

import com.zhoubyte.scorpioaspose.dto.DocumentDto;

/**
 * 文档在线编辑：Word 与 HTML 互转
 */
public interface DocumentEditService {

    /**
     * 将 Word 文档转为可编辑 HTML（body 内片段）
     */
    String toHtml(String fileId);

    /**
     * 将编辑后的 HTML 写回为 Word 文档
     */
    DocumentDto saveHtml(String fileId, String html);
}
