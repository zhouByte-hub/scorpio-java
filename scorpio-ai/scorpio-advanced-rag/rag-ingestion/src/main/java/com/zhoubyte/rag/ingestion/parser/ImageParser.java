package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.Block;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 图片解析器：视觉模型（Ollama qwen2.5vl 等）生成图片内容描述文字。
 *
 * <p>流程图、组织架构图、扫描件截图等无法通过文本抽取获得的信息，
 * 借助 VLM 转成可检索的文字描述入库。</p>
 */
@Component
public class ImageParser implements DocumentParser {

    private static final Set<String> SUPPORTED = Set.of("png", "jpg", "jpeg", "webp", "bmp", "gif");
    private static final Map<String, String> MIME_TYPES = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "webp", "image/webp",
            "bmp", "image/bmp",
            "gif", "image/gif");

    private static final String PROMPT = """
            请用中文客观描述这张图片的内容，供知识库检索使用。要求：
            1. 如果是流程图/架构图/组织图，按节点和连线关系逐项说明；
            2. 如果是表格截图，用文字逐行转述表格内容；
            3. 如果含文字，逐字转述关键文字；
            4. 不要猜测图中没有的信息，不要发散评价。
            """;

    private final ChatClient visionClient;

    public ImageParser(ChatClient visionClient) {
        this.visionClient = visionClient;
    }

    @Override
    public boolean supports(String extension) {
        return SUPPORTED.contains(extension);
    }

    @Override
    public ParsedDocument parse(byte[] data, String fileName) {
        String extension = extension(fileName);
        Media media = Media.builder()
                .mimeType(MimeType.valueOf(MIME_TYPES.get(extension)))
                .data(new ByteArrayResource(data))
                .build();

        String description = visionClient.prompt()
                .user(u -> u.text(PROMPT).media(media))
                .call()
                .content();
        if (description == null || description.isBlank()) {
            throw new IllegalStateException("视觉模型未返回图片描述: " + fileName);
        }
        String title = fileName.replaceAll("\\.[^.]+$", "");
        return new ParsedDocument(title, List.of(Block.image("[图片描述] " + description.strip(), null)));
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!SUPPORTED.contains(extension)) {
            throw new IllegalArgumentException("不支持的图片格式: " + fileName);
        }
        return extension;
    }
}
