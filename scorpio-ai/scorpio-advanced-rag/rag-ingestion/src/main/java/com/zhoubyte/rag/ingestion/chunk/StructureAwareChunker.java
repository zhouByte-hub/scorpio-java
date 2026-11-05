package com.zhoubyte.rag.ingestion.chunk;

import com.zhoubyte.rag.ingestion.block.Block;
import com.zhoubyte.rag.ingestion.block.BlockType;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import com.zhoubyte.rag.ingestion.config.RagIngestionProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 结构感知分块器。
 *
 * <p>与朴素的滑动窗口切分相比，本实现针对"文档有结构"这一事实做三件事：
 * <ol>
 *   <li>标题树：TITLE 块维护章节栈，每个 chunk 头部前置"章节路径"，让短 chunk
 *       自带上下文（"住宿标准"单独一句话毫无信息量，但"差旅制度 > 第三章 > 住宿标准"就有）；</li>
 *   <li>表格整块：TABLE 块永不参与字符切分，整表一个 chunk——报销标准、参数表
 *       被切碎后任何一路召回都拼不回完整语义；</li>
 *   <li>图片独立块：IMAGE 块（VLM 描述）独立成 chunk，不与正文混淆。</li>
 * </ol>
 * 超长 TEXT 块先按段落贪心打包，段落本身超限时再滑动窗口切分（带 overlap）。</p>
 */
@Component
public class StructureAwareChunker {

    private final RagIngestionProperties properties;

    public StructureAwareChunker(RagIngestionProperties properties) {
        this.properties = properties;
    }

    public List<ChunkResult> chunk(ParsedDocument document) {
        List<ChunkResult> results = new ArrayList<>();
        Deque<int[]> titleStack = new ArrayDeque<>(); // [level, titleIndex]
        List<String> titles = new ArrayList<>();
        int position = 0;

        for (Block block : document.blocks()) {
            if (block.type() == BlockType.TITLE) {
                while (!titleStack.isEmpty() && titleStack.peek()[0] >= block.level()) {
                    titleStack.pop();
                }
                titles.add(block.text());
                titleStack.push(new int[]{block.level(), titles.size() - 1});
                continue;
            }
            String sectionPath = sectionPath(titleStack, titles);
            position++;
            switch (block.type()) {
                case TABLE -> results.add(new ChunkResult(
                        sectionPrefix(sectionPath) + truncate(block.text(), properties.maxTableChars()),
                        BlockType.TABLE.name(), sectionPath, block.page(), position));
                case IMAGE -> results.add(new ChunkResult(
                        sectionPrefix(sectionPath) + block.text(),
                        BlockType.IMAGE.name(), sectionPath, block.page(), position));
                default -> {
                    String text = block.text().trim();
                    if (text.isEmpty()) {
                        position--;
                        continue;
                    }
                    for (String piece : splitText(text, properties.chunkSize(), properties.overlap())) {
                        results.add(new ChunkResult(sectionPrefix(sectionPath) + piece,
                                BlockType.TEXT.name(), sectionPath, block.page(), position));
                    }
                }
            }
        }
        return results;
    }

    private String sectionPath(Deque<int[]> stack, List<String> titles) {
        if (stack.isEmpty()) {
            return "";
        }
        return stack.stream()
                .map(entry -> titles.get(entry[1]))
                .reduce((a, b) -> a + " > " + b)
                .orElse("");
    }

    private String sectionPrefix(String sectionPath) {
        return sectionPath.isEmpty() ? "" : "【" + sectionPath + "】\n";
    }

    private String truncate(String text, int maxChars) {
        return text.length() <= maxChars ? text : text.substring(0, maxChars) + "\n…（表格超长已截断）";
    }

    /**
     * TEXT 块分割：不超过目标块长直接返回；否则按段落贪心打包，
     * 单段仍超限时滑动窗口切分（步长 = chunkSize - overlap）。
     */
    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> pieces = new ArrayList<>();
        if (text.length() <= chunkSize) {
            pieces.add(text);
            return pieces;
        }
        StringBuilder packed = new StringBuilder();
        for (String paragraph : text.split("\n+")) {
            if (paragraph.isBlank()) {
                continue;
            }
            if (paragraph.length() > chunkSize) {
                flushPacked(packed, pieces, chunkSize);
                for (int start = 0; start < paragraph.length(); start += Math.max(chunkSize - overlap, 1)) {
                    pieces.add(paragraph.substring(start, Math.min(start + chunkSize, paragraph.length())));
                    if (start + chunkSize >= paragraph.length()) {
                        break;
                    }
                }
                continue;
            }
            if (packed.length() + paragraph.length() + 1 > chunkSize) {
                flushPacked(packed, pieces, chunkSize);
            }
            if (packed.length() > 0) {
                packed.append('\n');
            }
            packed.append(paragraph);
        }
        flushPacked(packed, pieces, chunkSize);
        return pieces;
    }

    private void flushPacked(StringBuilder packed, List<String> pieces, int chunkSize) {
        if (packed.isEmpty()) {
            return;
        }
        // 贪心打包可能略超 chunkSize（单段落接近上限时），保证不空切即可
        pieces.add(packed.length() <= chunkSize ? packed.toString()
                : packed.substring(0, chunkSize));
        packed.setLength(0);
    }
}
