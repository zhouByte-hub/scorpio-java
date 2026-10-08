package com.zhoubyte.alibabacore.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 天气查询工具（模拟数据）。
 *
 * <p>@Tool 标注的方法会被框架转换为 ToolDefinition（名称、描述、JSON Schema），
 * 随请求发送给模型；模型判断需要时返回 tool_call，由 Spring AI 执行本地方法
 * 并把结果回填给模型继续生成最终回答（Function Calling 流程）。</p>
 */
@Component
public class WeatherTools {

    /** 模拟的城市天气数据 */
    private static final Map<String, String> MOCK_WEATHER = Map.of(
            "北京", "晴，8℃，西北风3级",
            "上海", "多云，15℃，东南风2级",
            "杭州", "小雨，13℃，微风",
            "广州", "雷阵雨，22℃，南风2级");

    @Tool(description = "查询指定城市的实时天气，输入城市名，返回温度、天气状况与风力")
    public String getWeather(
            @ToolParam(description = "城市名称，例如：北京、杭州") String city) {
        return MOCK_WEATHER.getOrDefault(city, city + "：暂无天气数据");
    }
}
