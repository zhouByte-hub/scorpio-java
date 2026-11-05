package com.zhoubyte.rag.ingestion.parser;

import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 解析路由器：按文件扩展名挑选支持的解析器。
 *
 * <p>同一路径存在多个候选时（如 PDF 的 Docling 与 PDFBox）按 {@code @Order}
 * 依次尝试，某个解析器失败自动切换下一个，全部失败才向上抛错。</p>
 */
@Component
public class ParserRouter {

    private static final Logger log = LoggerFactory.getLogger(ParserRouter.class);

    private final List<DocumentParser> parsers;

    public ParserRouter(List<DocumentParser> parsers) {
        this.parsers = parsers;
    }

    public ParsedDocument parse(byte[] data, String fileName) {
        String extension = extension(fileName);
        List<DocumentParser> candidates = parsers.stream()
                .filter(p -> p.supports(extension))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "暂不支持的文件类型 ." + extension + "，支持：md/html/docx/xlsx/pptx/pdf/图片");
        }
        Exception lastError = null;
        for (DocumentParser parser : candidates) {
            try {
                ParsedDocument parsed = parser.parse(data, fileName);
                if (parsed != null && !parsed.blocks().isEmpty()) {
                    return parsed;
                }
                lastError = new IllegalStateException(parser.getClass().getSimpleName() + " 解析结果为空");
            } catch (Exception e) {
                lastError = e;
                log.warn("解析器 {} 处理 {} 失败，尝试下一个候选：{}",
                        parser.getClass().getSimpleName(), fileName, e.getMessage());
            }
        }
        throw new IllegalStateException("文件解析失败: " + fileName, lastError);
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("文件名缺少扩展名: " + fileName);
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 当前支持的扩展名集合（供错误提示与前端展示）。
     */
    public List<String> supportedExtensions() {
        return Arrays.asList("md", "markdown", "html", "htm", "docx", "xlsx", "xlsm", "pptx", "pdf",
                "png", "jpg", "jpeg", "webp", "bmp", "gif");
    }
}
