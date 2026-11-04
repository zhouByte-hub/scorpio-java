package com.zhoubyte.rag.pipeline.model;

/**
 * 前置处理结果：意图分类 + 结构化过滤条件。
 *
 * @param cleanedQuery 清洗后的 Query
 * @param intent       意图：RETRIEVAL（需要检索）/ CHITCHAT（寒暄，直接回答）/ OUT_OF_SCOPE（超出知识范围）
 * @param filters      从 Query 中抽取的元数据过滤条件，可为空字段
 */
public record PreprocessResult(String cleanedQuery, String intent, Filters filters) {

    /**
     * 结构化过滤条件，null 表示该维度不限制。
     *
     * @param department 部门
     * @param level      密级（取最高密级，检索时允许该级别及以下）
     * @param docType    文档类型
     * @param year       年份（yyyy）
     */
    public record Filters(String department, String level, String docType, String year) {
    }
}
