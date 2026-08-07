package com.zhoubyte.scorpioelastic.dto.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchUserDocumentRequest(
        @Valid
        @NotEmpty(message = "用户文档不能为空")
        List<UserDocumentRequest> documents
) {
}
