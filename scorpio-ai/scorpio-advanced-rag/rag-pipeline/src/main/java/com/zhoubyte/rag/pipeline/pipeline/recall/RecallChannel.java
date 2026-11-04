package com.zhoubyte.rag.pipeline.pipeline.recall;

import com.zhoubyte.rag.pipeline.model.RecallResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 召回通道：每路独立打分，结果交给 RRF 融合，通道之间分数不可直接比较。
 *
 * <p>{@code allowed} 为 RoaringBitmap 元数据过滤解析出的 seq 白名单：
 * empty 表示无过滤限制；非 empty 时作为 ES terms 过滤条件下推到本路查询中。</p>
 */
public interface RecallChannel {

    /**
     * 通道名：bm25 / sparse / dense。
     */
    String name();

    /**
     * 按查询召回 topK 个候选。
     */
    List<RecallResult> recall(String query, Optional<Set<Integer>> allowed, int topK);
}
