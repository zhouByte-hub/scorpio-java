package com.zhoubyte.alibabacore.model;

import java.util.List;

/**
 * 结构化输出目标类型：电影推荐。
 * 使用 Java record 声明，Spring AI 会根据字段名与类型自动生成 JSON Schema 约束模型输出。
 *
 * @param title    电影名称
 * @param director 导演
 * @param year     上映年份
 * @param tags     标签列表
 * @param reason   推荐理由
 */
public record MovieRecommendation(String title,
                                  String director,
                                  int year,
                                  List<String> tags,
                                  String reason) {
}
