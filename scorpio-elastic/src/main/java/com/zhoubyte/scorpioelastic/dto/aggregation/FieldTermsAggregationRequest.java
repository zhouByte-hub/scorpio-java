package com.zhoubyte.scorpioelastic.dto.aggregation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record FieldTermsAggregationRequest(
        @NotBlank(message = "索引名称不能为空")
        String indexName,

        @NotBlank(message = "字段名称不能为空")
        String fieldName,

        @Min(value = 1, message = "返回数量不能小于 1")
        @Max(value = 100, message = "返回数量不能大于 100")
        Integer size
) {
    public int actualSize() {
        return size == null ? 10 : size;
    }
}
