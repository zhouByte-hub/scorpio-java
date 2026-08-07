package com.zhoubyte.scorpioelastic.dto.document;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DynamicSearchRequest(
        @NotBlank(message = "索引名称不能为空")
        String indexName,

        @NotBlank(message = "搜索关键词不能为空")
        String keyword,

        @Min(value = 0, message = "页码不能小于 0")
        Integer page,

        @Min(value = 1, message = "每页条数不能小于 1")
        @Max(value = 100, message = "每页条数不能大于 100")
        Integer size
) {
    public int actualPage() {
        return page == null ? 0 : page;
    }

    public int actualSize() {
        return size == null ? 10 : size;
    }
}
