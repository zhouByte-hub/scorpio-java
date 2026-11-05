package com.zhoubyte.rag.ingestion.chunk;

/**
 * 结构感知分块结果。
 *
 * @param content     chunk 正文（TEXT 块已前置章节路径）
 * @param blockType   来源块类型（TEXT/TABLE/IMAGE）
 * @param sectionPath 标题树章节路径（如 "第一章 > 报销标准"）
 * @param page        页码（无页概念的格式为 null）
 * @param position    在文档内的顺序（从 1 开始）
 */
public record ChunkResult(String content, String blockType, String sectionPath, Integer page, int position) {
}
