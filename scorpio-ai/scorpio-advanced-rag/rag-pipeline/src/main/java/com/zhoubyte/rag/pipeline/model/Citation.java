package com.zhoubyte.rag.pipeline.model;

/**
 * 引用来源：答案中 [n] 锚点对应的原始 chunk 溯源信息。
 *
 * @param anchor   答案中的引用编号，如 1 表示 [1]
 * @param docId    文档 ID
 * @param title    文档标题
 * @param page     页码
 * @param position 片段位置（chunk 序号，从 1 开始）
 * @param snippet  原文片段摘录
 */
public record Citation(int anchor, String docId, String title, int page, int position, String snippet) {
}
