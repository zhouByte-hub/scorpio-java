package com.zhoubyte.rag.pipeline.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * LLM 输出 JSON 解析工具：小参数量本地模型经常在 JSON 外包裹 ```json 代码围栏或额外文字，
 * 解析前先截取首个 '{' 或 '[' 到最后一个对应闭合符之间的内容。
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    /**
     * 从 LLM 原始输出中提取并解析 JSON 对象。
     */
    public static <T> T parseObject(String raw, Class<T> type) {
        try {
            return MAPPER.readValue(extractJson(raw), type);
        } catch (Exception e) {
            throw new IllegalStateException("LLM 输出 JSON 解析失败: " + raw, e);
        }
    }

    /**
     * 从 LLM 原始输出中提取并解析 JSON 数组。
     */
    public static <T> T parseArray(String raw, TypeReference<T> type) {
        try {
            return MAPPER.readValue(extractJson(raw), type);
        } catch (Exception e) {
            throw new IllegalStateException("LLM 输出 JSON 解析失败: " + raw, e);
        }
    }

    /**
     * 截取首个 '{' / '[' 到最后一个 '}' / ']' 之间的内容，去掉代码围栏与解释文字。
     */
    public static String extractJson(String raw) {
        if (raw == null) {
            throw new IllegalStateException("LLM 输出为空");
        }
        String text = raw.trim();
        int objStart = text.indexOf('{');
        int arrStart = text.indexOf('[');
        boolean objFirst = objStart >= 0 && (arrStart < 0 || objStart < arrStart);
        int start = objFirst ? objStart : arrStart;
        if (start < 0) {
            throw new IllegalStateException("LLM 输出中不含 JSON: " + raw);
        }
        char open = objFirst ? '{' : '[';
        char close = objFirst ? '}' : ']';
        int end = text.lastIndexOf(close);
        if (end <= start) {
            throw new IllegalStateException("LLM 输出中 JSON 不完整: " + raw);
        }
        return text.substring(start, end + 1);
    }
}
