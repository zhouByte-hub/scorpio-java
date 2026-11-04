package com.zhoubyte.rag.pipeline.pipeline.assemble;

import com.zhoubyte.rag.pipeline.model.Citation;
import com.zhoubyte.rag.pipeline.model.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 【上下文组装 + 溯源锚定】。
 *
 * <p>进入 Prompt 的每个 chunk 都绑定文档 ID、页码、片段位置并编号（[1]、[2]…），
 * 生成阶段强制要求答案使用 [n] 锚点引用依据；回答完成后解析答案中的锚点，
 * 还原出实际被引用的原文片段，形成可溯源的 Citation 列表。</p>
 */
@Component
public class ContextAssembler {

    private static final Pattern ANCHOR_PATTERN = Pattern.compile("\\[(\\d{1,2})]");
    private static final int SNIPPET_LENGTH = 120;

    /**
     * 将重排后的 chunk 组装为带编号锚点的上下文文本。
     */
    public AssembledContext assemble(List<DocumentChunk> chunks) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            builder.append("[%d]《%s》（docId=%s，第%d页，片段%d）\n%s\n\n"
                    .formatted(i + 1, chunk.title(), chunk.docId(), chunk.page(), chunk.position(), chunk.content()));
        }
        return new AssembledContext(builder.toString().trim(), chunks);
    }

    /**
     * 解析答案中的 [n] 锚点，生成实际被引用的来源列表。
     */
    public List<Citation> extractCitations(String answer, List<DocumentChunk> chunks) {
        SortedSet<Integer> anchors = new TreeSet<>();
        Matcher matcher = ANCHOR_PATTERN.matcher(answer == null ? "" : answer);
        while (matcher.find()) {
            int anchor = Integer.parseInt(matcher.group(1));
            if (anchor >= 1 && anchor <= chunks.size()) {
                anchors.add(anchor);
            }
        }
        List<Citation> citations = new ArrayList<>();
        for (int anchor : anchors) {
            DocumentChunk chunk = chunks.get(anchor - 1);
            String snippet = chunk.content();
            if (snippet.length() > SNIPPET_LENGTH) {
                snippet = snippet.substring(0, SNIPPET_LENGTH) + "…";
            }
            citations.add(new Citation(anchor, chunk.docId(), chunk.title(), chunk.page(), chunk.position(), snippet));
        }
        return citations;
    }
}
