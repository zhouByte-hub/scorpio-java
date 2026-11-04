package com.zhoubyte.rag.pipeline.pipeline.assemble;

import com.zhoubyte.rag.pipeline.model.DocumentChunk;

import java.util.List;

/**
 * 上下文组装结果。
 *
 * @param contextText 注入 Prompt 的带锚点上下文文本
 * @param chunks      锚点编号对应的 chunk（下标 i 即锚点 i+1）
 */
public record AssembledContext(String contextText, List<DocumentChunk> chunks) {
}
