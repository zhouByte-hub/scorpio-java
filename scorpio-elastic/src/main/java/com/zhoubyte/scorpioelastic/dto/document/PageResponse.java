package com.zhoubyte.scorpioelastic.dto.document;

import java.util.List;

public record PageResponse<T>(
        Long total,
        Integer page,
        Integer size,
        List<T> content
) {
}
