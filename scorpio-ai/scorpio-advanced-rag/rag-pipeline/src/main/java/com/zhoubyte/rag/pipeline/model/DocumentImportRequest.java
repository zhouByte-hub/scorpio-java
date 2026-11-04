package com.zhoubyte.rag.pipeline.model;

/**
 * 文档导入请求。
 *
 * @param title     文档标题
 * @param content   文档正文
 * @param metadata  文档元数据
 * @param chunkSize 分块目标长度（字符），null 时使用默认 300
 * @param overlap   相邻分块重叠长度（字符），null 时使用默认 60
 */
public record DocumentImportRequest(String title, String content, MetadataInfo metadata,
                                    Integer chunkSize, Integer overlap) {
}
