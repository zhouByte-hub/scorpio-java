package com.zhoubyte.rag.pipeline.pipeline.preprocess;

import com.zhoubyte.rag.pipeline.model.PreprocessResult;
import com.zhoubyte.rag.pipeline.util.JsonUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 【前置处理层】Query 清洗 + 意图分类 + 元数据过滤条件抽取。
 *
 * <p>一次 LLM 调用同时完成三件事：
 * <ol>
 *   <li>清洗：去掉口语冗余、修正错别字，得到规范的检索主 Query；</li>
 *   <li>意图分类：RETRIEVAL（需检索）/ CHITCHAT（寒暄或通用常识，无需检索）；</li>
 *   <li>抽取结构化过滤条件（部门/密级/文档类型/年份），交给 {@link MetadataFilterResolver}
 *       用 RoaringBitmap 做集合过滤。</li>
 * </ol>
 * </p>
 */
@Component
public class QueryPreProcessor {

    private final ChatClient utilityClient;

    public QueryPreProcessor(ChatClient utilityClient) {
        this.utilityClient = utilityClient;
    }

    public PreprocessResult process(String rawQuery) {
        String cleaned = clean(rawQuery);
        String prompt = """
                你是企业知识库检索系统的查询理解模块。对用户查询完成两件事：
                1. 判断意图：
                   - RETRIEVAL：需要检索企业知识库文档才能回答的问题
                   - CHITCHAT：寒暄、问候、或与知识库无关的通用常识问题
                2. 抽取元数据过滤条件（查询中没有的维度填 null，不要臆造）：
                   - department：部门，如 研发部、财务部、人力资源部
                   - level：密级上限，只能是 public / internal / secret 之一
                   - docType：文档类型，如 规章制度、技术方案、会议纪要
                   - year：年份，4 位数字
                只输出 JSON，不要解释：
                {"cleanedQuery":"清洗后的规范查询","intent":"RETRIEVAL或CHITCHAT","filters":{"department":null,"level":null,"docType":null,"year":null}}

                用户查询：%s""".formatted(cleaned);

        String output = utilityClient.prompt().user(prompt).call().content();
        PreprocessResult result = JsonUtils.parseObject(output, PreprocessResult.class);
        return new PreprocessResult(result.cleanedQuery() == null ? cleaned : result.cleanedQuery(),
                result.intent(), normalize(result.filters()));
    }

    /**
     * 规则清洗：压缩空白、去首尾标点。
     */
    private String clean(String raw) {
        String text = raw == null ? "" : raw.replaceAll("\\s+", " ").trim();
        return text.replaceAll("^[，。！？、,.!?\\s]+|[，。！？、,.!?\\s]+$", "");
    }

    /**
     * 兼容小模型把 null 输出成字符串 "null" 的情况。
     */
    private PreprocessResult.Filters normalize(PreprocessResult.Filters filters) {
        if (filters == null) {
            return new PreprocessResult.Filters(null, null, null, null);
        }
        return new PreprocessResult.Filters(
                nullToNull(filters.department()),
                nullToNull(filters.level()),
                nullToNull(filters.docType()),
                nullToNull(filters.year()));
    }

    private String nullToNull(String value) {
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim()) ? null : value.trim();
    }
}
