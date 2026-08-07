package com.zhoubyte.scorpioelastic.dto.aggregation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AgeRangeRequest(
        @NotBlank(message = "区间名称不能为空")
        String name,

        @NotNull(message = "区间最小值不能为空")
        Integer min,

        @NotNull(message = "区间最大值不能为空")
        Integer max
) {
}
