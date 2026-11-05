package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.Block;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 降级解析器（PDFBox）：逐页抽取文本层，保留页码。
 *
 * <p>仅当 Docling 服务不可用时启用（{@code @Order(2)}）。抽取不到文本层说明
 * 大概率是扫描件，直接抛错提示走 OCR/视觉方案，避免把空 chunk 入库。</p>
 */
@Order(2)
@Component
public class PdfFallbackParser implements DocumentParser {

    private static final Logger log = LoggerFactory.getLogger(PdfFallbackParser.class);

    @Override
    public boolean supports(String extension) {
        return extension.equals("pdf");
    }

    @Override
    public ParsedDocument parse(byte[] data, String fileName) throws Exception {
        try (PDDocument pdf = PDDocument.load(data)) {
            List<Block> blocks = new ArrayList<>();
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(pdf).strip();
                if (!text.isEmpty()) {
                    blocks.add(Block.text(text, page));
                }
            }
            if (blocks.isEmpty()) {
                throw new IllegalStateException(
                        "PDF 无文本层（疑似扫描件），PDFBox 降级通道无法解析，请启动 Docling 服务或接入 OCR");
            }
            log.warn("PDF 使用 PDFBox 降级通道解析（Docling 未启用），文件：{}，页数：{}", fileName, blocks.size());
            return new ParsedDocument(fileName.replaceAll("\\.[^.]+$", ""), blocks);
        }
    }
}
