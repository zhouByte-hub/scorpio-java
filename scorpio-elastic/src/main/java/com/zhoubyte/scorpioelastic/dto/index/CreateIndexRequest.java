package com.zhoubyte.scorpioelastic.dto.index;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateIndexRequest(
        @NotBlank(message = "索引名称不能为空")
        String indexName,

        @Valid
        @NotEmpty(message = "索引字段不能为空")
        List<IndexFieldRequest> fields
) {
}
