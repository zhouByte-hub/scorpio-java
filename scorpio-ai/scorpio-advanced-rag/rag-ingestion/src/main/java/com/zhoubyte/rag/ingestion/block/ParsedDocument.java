package com.zhoubyte.rag.ingestion.block;

import java.util.List;

/**
 * 解析结果：文档标题 + 有序 Block 流。
 *
 * @param title  文档标题（取首个标题块，缺省时由文件名兜底）
 * @param blocks 有序内容块
 */
public record ParsedDocument(String title, List<Block> blocks) {
}
