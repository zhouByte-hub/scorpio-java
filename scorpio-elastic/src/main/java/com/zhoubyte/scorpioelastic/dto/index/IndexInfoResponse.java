package com.zhoubyte.scorpioelastic.dto.index;

import java.util.Map;

public record IndexInfoResponse(
        String indexName,
        Boolean exists,
        Map<String, Object> mapping
) {
}
