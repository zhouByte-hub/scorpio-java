package com.zhoubyte.rag.observability.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具：演示最简单的工具调用 Span。
 */
@Component
public class TimeTools {

    @Tool(description = "获取当前日期时间，可指定时区偏移，如 +8 表示东八区，默认 +8")
    public String now(@ToolParam(description = "UTC 时区偏移小时数，例如 8", required = false) Integer utcOffset) {
        int offset = utcOffset == null ? 8 : utcOffset;
        return LocalDateTime.now().plusHours(offset - 8)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
