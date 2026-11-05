package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.Block;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import com.zhoubyte.rag.ingestion.util.MarkdownTables;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * HTML 解析器（jsoup）：标题层级、段落、表格、图片 alt 信息结构化。
 */
@Component
public class HtmlParser implements DocumentParser {

    @Override
    public boolean supports(String extension) {
        return extension.equals("html") || extension.equals("htm");
    }

    @Override
    public ParsedDocument parse(byte[] data, String fileName) {
        Document dom = Jsoup.parse(new String(data, StandardCharsets.UTF_8));
        List<Block> blocks = new ArrayList<>();
        String title = dom.title().isBlank() ? null : dom.title().trim();

        walk(dom.body(), blocks);

        if (title == null) {
            title = blocks.stream()
                    .filter(b -> b.type() == com.zhoubyte.rag.ingestion.block.BlockType.TITLE)
                    .map(com.zhoubyte.rag.ingestion.block.Block::text)
                    .findFirst().orElse(fileName);
        }
        return new ParsedDocument(title, blocks);
    }

    private void walk(Element element, List<Block> blocks) {
        for (Element child : element.children()) {
            String tag = child.tagName().toLowerCase();
            switch (tag) {
                case "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    String text = child.text().trim();
                    if (!text.isEmpty()) {
                        blocks.add(Block.title(text, Integer.parseInt(tag.substring(1)), null));
                    }
                }
                case "p", "li", "pre", "blockquote" -> {
                    String text = child.text().trim();
                    if (!text.isEmpty()) {
                        blocks.add(Block.text(text, null));
                    }
                }
                case "table" -> blocks.add(Block.table(tableToMarkdown(child), null));
                case "img" -> {
                    String alt = child.attr("alt").trim();
                    if (!alt.isEmpty()) {
                        blocks.add(Block.image("[图片] " + alt, null));
                    }
                }
                default -> walk(child, blocks);
            }
        }
    }

    /**
     * HTML 表格转 Markdown（thead 优先，否则第一行作为表头）。
     */
    private String tableToMarkdown(Element table) {
        List<String> headCells = table.select("thead th, thead td").stream()
                .map(Element::text).map(String::trim).toList();
        List<List<String>> bodyRows = table.select("tbody tr").stream()
                .map(tr -> tr.select("th, td").stream().map(c -> c.text().trim()).toList())
                .toList();

        List<String> headers;
        List<List<String>> rows;
        if (!headCells.isEmpty()) {
            headers = headCells;
            rows = new ArrayList<>(bodyRows);
        } else if (!bodyRows.isEmpty()) {
            headers = bodyRows.getFirst();
            rows = new ArrayList<>(bodyRows.subList(1, bodyRows.size()));
        } else {
            return "";
        }
        return MarkdownTables.toMarkdown(headers, rows);
    }
}
