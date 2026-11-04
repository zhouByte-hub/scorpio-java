package com.zhoubyte.rag.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 流水线执行轨迹：记录每个阶段的输入/输出摘要与耗时，便于观测各层效果。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineTrace {

    private final List<Stage> stages = new ArrayList<>();

    public void add(String stage, long costMs, String detail) {
        stages.add(new Stage(stage, costMs, detail));
    }

    public List<Stage> getStages() {
        return stages;
    }

    /**
     * 单个阶段轨迹。
     *
     * @param stage  阶段名
     * @param costMs 耗时（毫秒）
     * @param detail 摘要（如召回通道命中数、重排分数、校验结论）
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stage(String stage, long costMs, String detail) {
    }
}
