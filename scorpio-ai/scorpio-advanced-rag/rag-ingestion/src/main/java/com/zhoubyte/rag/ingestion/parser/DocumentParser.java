package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.ParsedDocument;

/**
 * 文档解析器 SPI：按文件扩展名路由，每种格式一个实现。
 */
public interface DocumentParser {

    /**
     * 是否支持该扩展名（小写，不含点）。
     */
    boolean supports(String extension);

    /**
     * 解析文件字节为统一的 Block 流。
     *
     * @param data     文件字节
     * @param fileName 原始文件名（含扩展名）
     */
    ParsedDocument parse(byte[] data, String fileName) throws Exception;
}
