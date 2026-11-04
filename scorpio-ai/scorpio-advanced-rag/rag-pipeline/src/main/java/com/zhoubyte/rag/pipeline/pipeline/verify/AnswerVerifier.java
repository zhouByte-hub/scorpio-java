package com.zhoubyte.rag.pipeline.pipeline.verify;

import com.zhoubyte.rag.pipeline.util.JsonUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 【校验层】LLM 事实校验（强准确率杀手锏）。
 *
 * <p>答案生成后，用独立低温 LLM 逐条核对：
 * <ol>
 *   <li>答案中的每个论断是否都能在选中的 chunk 里找到直接依据；</li>
 *   <li>是否存在跨文档脑补、推断原文没有的信息。</li>
 * </ol>
 * 校验不通过则放弃该答案，强制返回“资料不足，无法回答”，
 * 宁可不答也不给不可信的答案。</p>
 */
@Component
public class AnswerVerifier {

    private final ChatClient verifyClient;

    public AnswerVerifier(ChatClient verifyClient) {
        this.verifyClient = verifyClient;
    }

    /**
     * 校验答案是否有据可依。
     */
    public Verdict verify(String question, String answer, String context) {
        String prompt = """
                你是严格的事实核查员。给定用户问题、候选资料和基于资料生成的答案，
                逐条核对答案中的每个论断：
                1. 是否能在候选资料中找到直接、明确的依据（允许同义转述，不允许延伸推断）；
                2. 是否存在资料中没有的、由模型脑补/跨文档拼接的内容。
                只要答案中存在任何一条无法找到依据的论断，grounded 必须为 false。
                只输出 JSON：{"grounded":true或false,"reason":"不通过时指出具体哪条论断无依据"}

                用户问题：%s

                候选资料：
                %s

                待校验答案：%s""".formatted(question, context, answer);

        String output = verifyClient.prompt().user(prompt).call().content();
        Verdict verdict = JsonUtils.parseObject(output, Verdict.class);
        return new Verdict(verdict.grounded(), verdict.reason() == null ? "" : verdict.reason());
    }

    /**
     * 校验结论。
     *
     * @param grounded 答案是否全部有依据
     * @param reason   不通过的原因
     */
    public record Verdict(boolean grounded, String reason) {
    }
}
