package com.zhoubyte.rag.ingestion.block;

/**
 * 解析后的内容块类型。
 */
public enum BlockType {
    /** 标题，携带层级（1~6），用于构建章节路径 */
    TITLE,
    /** 正文段落 */
    TEXT,
    /** 表格（Markdown 表示，整表一块永不切碎） */
    TABLE,
    /** 图片（内容为视觉模型生成的文字描述） */
    IMAGE
}
