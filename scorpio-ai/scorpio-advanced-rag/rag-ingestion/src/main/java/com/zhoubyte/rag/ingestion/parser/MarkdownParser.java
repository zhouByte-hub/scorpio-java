package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.Block;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 解析器：按标题/表格/段落切分为 Block 流。
 *
 * <p>同时作为 Docling 输出结果（md_content）的二次结构化工具。</p>
 */
@Component
public class MarkdownParser implements DocumentParser {

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");

    @Override
    public boolean supports(String extension) {
        return extension.equals("md") || extension.equals("markdown");
    }

    @Override
    public ParsedDocument parse(byte[] data, String fileName) {
        return fromMarkdown(new String(data, StandardCharsets.UTF_8));
    }

    /**
     * Markdown 文本转 Block 流（Docling 等上游输出复用）。
     */
    public ParsedDocument fromMarkdown(String markdown) {
        List<Block> blocks = new ArrayList<>();
        String title = null;
        List<String> textBuffer = new ArrayList<>();
        List<String> tableBuffer = new ArrayList<>();
        boolean inCodeFence = false;

        for (String rawLine : markdown.split("\\R", -1)) {
            String line = rawLine.stripTrailing();
            String trimmed = line.trim();

            if (trimmed.startsWith("```")) {
                inCodeFence = !inCodeFence;
                textBuffer.add(line);
                continue;
            }

            if (!inCodeFence && trimmed.startsWith("|") && trimmed.endsWith("|")) {
                flushText(textBuffer, blocks, null);
                tableBuffer.add(trimmed);
                continue;
            }
            flushTable(tableBuffer, blocks, null);

            if (!inCodeFence) {
                Matcher heading = HEADING.matcher(trimmed);
                if (heading.matches()) {
                    flushText(textBuffer, blocks, null);
                    String headingText = heading.group(2).trim();
                    if (title == null) {
                        title = headingText;
                    }
                    blocks.add(Block.title(headingText, heading.group(1).length(), null));
                    continue;
                }
            }

            if (trimmed.isEmpty()) {
                flushText(textBuffer, blocks, null);
            } else {
                textBuffer.add(line);
            }
        }
        flushTable(tableBuffer, blocks, null);
        flushText(textBuffer, blocks, null);

        return new ParsedDocument(title, blocks);
    }

    private void flushText(List<String> buffer, List<Block> blocks, Integer page) {
        String text = String.join("\n", buffer).trim();
        if (!text.isEmpty()) {
            blocks.add(Block.text(text, page));
        }
        buffer.clear();
    }

    private void flushTable(List<String> buffer, List<Block> blocks, Integer page) {
        if (!buffer.isEmpty()) {
            blocks.add(Block.table(String.join("\n", buffer), page));
            buffer.clear();
        }
    }
}
