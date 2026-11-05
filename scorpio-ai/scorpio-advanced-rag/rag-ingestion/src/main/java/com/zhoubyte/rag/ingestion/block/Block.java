package com.zhoubyte.rag.ingestion.block;

/**
 * 解析中间表示：所有格式的文档统一转为 Block 流后再进入分块阶段。
 *
 * @param type  块类型
 * @param text  块文本（表格为 Markdown 表，图片为视觉模型描述）
 * @param level 标题层级（1~6），非标题块为 0
 * @param page  所在页码（PDF/PPT 有页概念，其他格式为 null）
 */
public record Block(BlockType type, String text, int level, Integer page) {

    public static Block title(String text, int level, Integer page) {
        return new Block(BlockType.TITLE, text, level, page);
    }

    public static Block text(String text, Integer page) {
        return new Block(BlockType.TEXT, text, 0, page);
    }

    public static Block table(String markdown, Integer page) {
        return new Block(BlockType.TABLE, markdown, 0, page);
    }

    public static Block image(String description, Integer page) {
        return new Block(BlockType.IMAGE, description, 0, page);
    }
}
