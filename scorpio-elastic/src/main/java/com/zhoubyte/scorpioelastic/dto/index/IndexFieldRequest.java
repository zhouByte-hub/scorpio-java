package com.zhoubyte.scorpioelastic.dto.index;

import jakarta.validation.constraints.NotBlank;

public record IndexFieldRequest(
        @NotBlank(message = "字段名称不能为空")
        String name,

        @NotBlank(message = "字段类型不能为空")
        String type,

        String analyzer,

        String searchAnalyzer
) {
}
