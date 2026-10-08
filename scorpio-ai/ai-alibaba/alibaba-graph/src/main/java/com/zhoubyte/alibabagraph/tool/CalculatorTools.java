package com.zhoubyte.alibabagraph.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 计算器工具集，供 ReactAgent 通过 Function Calling 调用。
 */
@Component
public class CalculatorTools {

    @Tool(description = "计算两个数的和")
    public double add(@ToolParam(description = "加数a") double a,
                      @ToolParam(description = "加数b") double b) {
        return a + b;
    }

    @Tool(description = "计算两个数的差（a 减 b）")
    public double subtract(@ToolParam(description = "被减数a") double a,
                           @ToolParam(description = "减数b") double b) {
        return a - b;
    }

    @Tool(description = "计算两个数的积")
    public double multiply(@ToolParam(description = "因数a") double a,
                           @ToolParam(description = "因数b") double b) {
        return a * b;
    }

    @Tool(description = "计算两个数的商（a 除以 b），b 不能为 0")
    public double divide(@ToolParam(description = "被除数a") double a,
                         @ToolParam(description = "除数b，不能为0") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为 0");
        }
        return a / b;
    }
}
