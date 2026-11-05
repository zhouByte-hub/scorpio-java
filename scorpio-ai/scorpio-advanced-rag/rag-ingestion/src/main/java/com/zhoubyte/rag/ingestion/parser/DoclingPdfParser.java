package com.zhoubyte.rag.ingestion.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import com.zhoubyte.rag.ingestion.config.RagIngestionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/**
 * PDF 首选解析器：调用 Docling 服务（docling-serve）将复杂版式 PDF
 * （表格/双栏/图文混排）转为 Markdown，再经 MarkdownParser 结构化。
 *
 * <p>{@code @Order(1)} 保证路由时优先于 PDFBox 降级通道（{@code @Order(2)}）；
 * 服务未启动/超时/解析失败时抛错，由 ParserRouter 自动切换降级通道。</p>
 */
@Order(1)
@Component
public class DoclingPdfParser implements DocumentParser {

    private static final Logger log = LoggerFactory.getLogger(DoclingPdfParser.class);

    private final MarkdownParser markdownParser;
    private final RagIngestionProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public DoclingPdfParser(MarkdownParser markdownParser, RagIngestionProperties properties) {
        this.markdownParser = markdownParser;
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public boolean supports(String extension) {
        return extension.equals("pdf");
    }

    @Override
    public ParsedDocument parse(byte[] data, String fileName) throws Exception {
        String boundary = "docling-" + UUID.randomUUID();
        String disposition = "Content-Disposition: form-data; name=\"files\"; filename=\""
                + fileName + "\"\r\nContent-Type: application/pdf\r\n\r\n";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.doclingBaseUrl() + "/v1/convert/source"))
                .timeout(Duration.ofSeconds(properties.doclingTimeoutSeconds()))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArrays(Arrays.asList(
                        ("--" + boundary + "\r\n" + disposition).getBytes(),
                        data,
                        ("\r\n--" + boundary + "--\r\n").getBytes())))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Docling 返回非 200：" + response.statusCode() + " " + response.body());
        }
        JsonNode root = objectMapper.readTree(response.body());
        String mdContent = root.path("document").path("md_content").asText("");
        if (mdContent.isBlank()) {
            throw new IllegalStateException("Docling 返回内容为空");
        }
        log.info("PDF 经 Docling 解析成功，文件：{}，Markdown 长度：{}", fileName, mdContent.length());
        return markdownParser.fromMarkdown(mdContent);
    }
}
