package com.zhoubyte.stream.tools;

import com.zhoubyte.stream.dto.WeatherResult;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTools {

    @McpTool(name = "get_weather", description = "查询指定城市的天气信息，返回城市、天气、温度和单位。")
    public WeatherResult getWeather(@McpToolParam(description = "城市名称，默认城市为深圳市") String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("city 不能为空");
        }
        return new WeatherResult(city.trim(), "晴", 26.5, "℃");
    }

}
