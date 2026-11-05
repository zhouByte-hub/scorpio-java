package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.Block;
import com.zhoubyte.rag.ingestion.block.BlockType;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import com.zhoubyte.rag.ingestion.util.MarkdownTables;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PowerPoint（.pptx）解析器（POI XSLF）：逐页提取标题、正文与表格，保留页码用于溯源。
 */
@Component
public class PowerPointParser implements DocumentParser {

    @Override
    public boolean supports(String extension) {
        return extension.equals("pptx");
    }

    @Override
    public ParsedDocument parse(byte[] data, String fileName) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(data))) {
            List<Block> blocks = new ArrayList<>();
            int page = 0;
            for (XSLFSlide slide : ppt.getSlides()) {
                page++;
                String title = slide.getTitle() != null ? slide.getTitle().trim() : "";
                if (!title.isEmpty()) {
                    blocks.add(Block.title(title, 1, page));
                }
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTable table) {
                        blocks.add(Block.table(tableToMarkdown(table), page));
                    } else if (shape instanceof XSLFTextShape textShape) {
                        String text = paragraphs(textShape.getTextParagraphs());
                        // 标题占位符已单独输出，跳过重复文本
                        if (!text.isEmpty() && !text.equals(title)) {
                            blocks.add(Block.text(text, page));
                        }
                    }
                }
            }
            String docTitle = blocks.stream()
                    .filter(b -> b.type() == BlockType.TITLE)
                    .map(Block::text)
                    .findFirst().orElse(fileName.replaceAll("\\.[^.]+$", ""));
            return new ParsedDocument(docTitle, blocks);
        }
    }

    private String paragraphs(List<XSLFTextParagraph> paragraphs) {
        List<String> lines = new ArrayList<>();
        for (XSLFTextParagraph paragraph : paragraphs) {
            String text = paragraph.getText().trim();
            if (!text.isEmpty()) {
                lines.add(text);
            }
        }
        return String.join("\n", lines);
    }

    private String tableToMarkdown(XSLFTable table) {
        List<String> headers = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        for (int r = 0; r < table.getNumberOfRows(); r++) {
            List<String> cells = new ArrayList<>();
            for (int c = 0; c < table.getNumberOfColumns(); c++) {
                cells.add(table.getCell(r, c).getText().trim());
            }
            if (r == 0) {
                headers.addAll(cells);
            } else {
                rows.add(cells);
            }
        }
        return headers.isEmpty() ? "" : MarkdownTables.toMarkdown(headers, rows);
    }
}
