package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.Block;
import com.zhoubyte.rag.ingestion.block.BlockType;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import com.zhoubyte.rag.ingestion.util.MarkdownTables;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word（.docx）解析器（POI XWPF）：保留段落顺序与标题层级，表格整表转 Markdown。
 */
@Component
public class WordParser implements DocumentParser {

    private static final Pattern HEADING_LEVEL = Pattern.compile("(?i)heading\\s*(\\d)");

    @Override
    public boolean supports(String extension) {
        return extension.equals("docx");
    }

    @Override
    public ParsedDocument parse(byte[] data, String fileName) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(data))) {
            List<Block> blocks = new ArrayList<>();
            for (IBodyElement element : doc.getBodyElements()) {
                if (element instanceof XWPFParagraph paragraph) {
                    appendParagraph(paragraph, blocks);
                } else if (element instanceof XWPFTable table) {
                    appendTable(table, blocks);
                }
            }
            String title = blocks.stream()
                    .filter(b -> b.type() == BlockType.TITLE)
                    .map(Block::text)
                    .findFirst().orElse(fileName);
            return new ParsedDocument(title, blocks);
        }
    }

    private void appendParagraph(XWPFParagraph paragraph, List<Block> blocks) {
        String text = paragraph.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        Integer level = headingLevel(paragraph);
        if (level != null) {
            blocks.add(Block.title(text, level, null));
        } else {
            blocks.add(Block.text(text, null));
        }
    }

    /**
     * 从段落样式推断标题层级（样式名 Heading 1~6 / 标题 1~6），非标题返回 null。
     */
    private Integer headingLevel(XWPFParagraph paragraph) {
        String style = paragraph.getStyleID() != null ? paragraph.getStyleID() : "";
        Matcher matcher = HEADING_LEVEL.matcher(style);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        String styleName = paragraph.getDocument().getStyles().getStyle(style) != null
                ? paragraph.getDocument().getStyles().getStyle(style).getName()
                : "";
        matcher = HEADING_LEVEL.matcher(styleName == null ? "" : styleName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private void appendTable(XWPFTable table, List<Block> blocks) {
        List<String> headers = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        List<XWPFTableRow> tableRows = table.getRows();

        for (int i = 0; i < tableRows.size(); i++) {
            List<String> cells = tableRows.get(i).getTableCells().stream()
                    .map(XWPFTableCell::getText).map(String::trim).toList();
            if (cells.isEmpty()) {
                continue;
            }
            if (i == 0) {
                headers.addAll(cells);
            } else {
                rows.add(cells);
            }
        }
        if (!headers.isEmpty()) {
            blocks.add(Block.table(MarkdownTables.toMarkdown(headers, rows), null));
        }
    }
}
