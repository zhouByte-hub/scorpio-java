package com.zhoubyte.scorpioelastic.dto.document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserDocumentRequest(
        String id,

        @NotBlank(message = "用户名不能为空")
        String username,

        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱不能为空")
        String email,

        @NotNull(message = "年龄不能为空")
        @Min(value = 0, message = "年龄不能小于 0")
        Integer age
) {
}
