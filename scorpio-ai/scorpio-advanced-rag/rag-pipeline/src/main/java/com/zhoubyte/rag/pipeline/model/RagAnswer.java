package com.zhoubyte.rag.pipeline.model;

import java.util.List;

/**
 * 最终问答结果。
 *
 * @param question  原始问题
 * @param answer    带引用锚点（[1][2]）的答案；资料不足时为兜底话术
 * @param citations 答案实际引用的来源列表
 * @param refused   是否因依据不足被校验层拦截（true 时 answer 为“资料不足”类兜底话术）
 * @param trace     流水线各阶段轨迹
 */
public record RagAnswer(String question, String answer, List<Citation> citations,
                        boolean refused, PipelineTrace trace) {
}
