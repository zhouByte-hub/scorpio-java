package com.zhoubyte.scorpioelastic.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record DynamicDocumentRequest(
        @NotBlank(message = "索引名称不能为空")
        String indexName,

        String id,

        @NotEmpty(message = "文档内容不能为空")
        Map<String, Object> content
) {
}
