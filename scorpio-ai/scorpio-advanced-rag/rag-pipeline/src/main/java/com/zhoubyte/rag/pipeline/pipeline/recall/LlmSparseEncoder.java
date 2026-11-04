package com.zhoubyte.rag.pipeline.pipeline.recall;

import com.zhoubyte.rag.pipeline.util.CjkTokenizer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 稀疏编码器默认实现：LLM 同义扩展 + IDF 加权。
 *
 * <p>让 LLM 列出与查询语义相同/相关的词项（如同义词、上/下位词、文档中常见的书面表达），
 * 将扩展词项与原查询词项合并后用语料 IDF 加权——权重体现“该词项对语料的区分度”，
 * 与 SPLADE 的“词项相关性权重”思想一致。</p>
 */
@Component
public class LlmSparseEncoder implements SparseEncoder {

    private final ChatClient utilityClient;
    private final IdfWeighter idfWeighter;

    public LlmSparseEncoder(ChatClient utilityClient, IdfWeighter idfWeighter) {
        this.utilityClient = utilityClient;
        this.idfWeighter = idfWeighter;
    }

    @Override
    public Map<String, Double> encode(String query) {
        Map<String, Double> sparse = new HashMap<>(idfWeighter.encodeIndex(query));
        String expansions = expand(query);
        for (String token : CjkTokenizer.tokenize(expansions)) {
            sparse.merge(token, idfWeighter.idf(token) * 0.8, Double::sum);
        }
        return sparse;
    }

    /**
     * LLM 同义/相关词扩展，失败时返回空串（退化为纯 IDF 加权的字面稀疏）。
     */
    private String expand(String query) {
        try {
            String prompt = """
                    列出与以下查询语义相同或高度相关的中文词项（同义词、近义词、文档中常见的书面表达），
                    最多 8 个，空格分隔，不要解释，不要输出标点。

                    查询：%s""".formatted(query);
            String output = utilityClient.prompt().user(prompt).call().content();
            return output == null ? "" : output.replaceAll("[\\p{Punct}，。、；：？！]", " ");
        } catch (Exception e) {
            return "";
        }
    }
}
