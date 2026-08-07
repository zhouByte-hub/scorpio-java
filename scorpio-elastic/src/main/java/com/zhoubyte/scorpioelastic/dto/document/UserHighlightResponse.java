package com.zhoubyte.scorpioelastic.dto.document;

import java.util.List;
import java.util.Map;

public record UserHighlightResponse(
        String id,
        String username,
        String email,
        Integer age,
        Map<String, List<String>> highlightFields
) {
}
