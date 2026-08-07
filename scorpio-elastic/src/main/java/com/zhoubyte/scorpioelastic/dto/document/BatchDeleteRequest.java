package com.zhoubyte.scorpioelastic.dto.document;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchDeleteRequest(
        @NotEmpty(message = "文档 ID 不能为空")
        List<String> ids
) {
}
