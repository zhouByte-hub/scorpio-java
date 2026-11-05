package com.zhoubyte.rag.ingestion.util;

import java.util.List;

/**
 * 二维数据转 Markdown 表格的工具（Word/Excel/PPT/HTML 表格的统一落地方言）。
 */
public final class MarkdownTables {

    private MarkdownTables() {
    }

    /**
     * 转为 Markdown 表格文本。
     *
     * @param headers 表头（第一行）
     * @param rows    数据行，可为空
     */
    public static String toMarkdown(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("| ");
        sb.append(String.join(" | ", escapeAll(headers)));
        sb.append(" |\n");
        sb.append("| ");
        sb.append(" | ".repeat(Math.max(headers.size(), 1)));
        sb.append(" |\n");
        for (List<String> row : rows) {
            sb.append("| ");
            sb.append(String.join(" | ", escapeAll(row)));
            sb.append(" |\n");
        }
        return sb.toString().stripTrailing();
    }

    private static List<String> escapeAll(List<String> cells) {
        return cells.stream().map(c -> c == null ? "" : c.replace("|", "\\|").replace("\n", " ")).toList();
    }
}
