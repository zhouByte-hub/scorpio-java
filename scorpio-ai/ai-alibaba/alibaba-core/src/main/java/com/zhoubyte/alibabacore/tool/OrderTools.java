package com.zhoubyte.alibabacore.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单查询工具（模拟数据），演示多工具组合调用。
 */
@Component
public class OrderTools {

    /** 模拟订单：订单号 -> 状态描述 */
    private static final Map<String, String> MOCK_ORDERS = Map.of(
            "SO20261001", "已发货，顺丰运单 SF1234567890，预计明日送达",
            "SO20261002", "待付款，订单金额 299.00 元",
            "SO20261003", "已完成，支持开具电子发票");

    @Tool(description = "根据订单号查询电商订单的当前状态与物流信息")
    public String queryOrder(
            @ToolParam(description = "订单编号，例如：SO20261001") String orderNo) {
        return MOCK_ORDERS.getOrDefault(orderNo, "未找到订单 " + orderNo);
    }

    @Tool(description = "查询指定城市的限行尾号规则，用于判断今天是否限行")
    public String getTrafficRestriction(
            @ToolParam(description = "城市名称") String city,
            @ToolParam(description = "星期几，取值：周一 到 周日") String weekday) {
        if ("北京".equals(city)) {
            return "北京" + weekday + "限行尾号：4 和 9";
        }
        return city + "暂未实施尾号限行政策";
    }
}
