package com.zhoubyte.alibabamcpserver.tools;

import com.zhoubyte.alibabamcpserver.dto.LogisticsInfo;
import com.zhoubyte.alibabamcpserver.dto.OrderInfo;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 电商 MCP Tool：订单查询与物流跟踪（模拟数据）。
 *
 * <p>@McpTool 方法会被 spring-ai-mcp-annotations 扫描并注册为 MCP 协议的
 * tools/list 与 tools/call 处理器，任何 MCP 客户端（不止 Spring AI）都能发现调用。</p>
 */
@Component
public class EcommerceTools {

    private static final Map<String, OrderInfo> ORDERS = Map.of(
            "SO20261001", new OrderInfo("SO20261001", "已发货", 299.00, "无线蓝牙耳机 ×1"),
            "SO20261002", new OrderInfo("SO20261002", "待付款", 1599.00, "机械键盘 ×1、鼠标垫 ×2"),
            "SO20261003", new OrderInfo("SO20261003", "已完成", 89.90, "手机壳 ×1"));

    private static final Map<String, LogisticsInfo> LOGISTICS = Map.of(
            "SO20261001", new LogisticsInfo("SO20261001", "顺丰速运", "SF1234567890",
                    List.of("【派送中】快递员正在为您派送",
                            "【运输中】快件已到达 杭州中转仓",
                            "【已揽收】深圳发货仓已揽收")));

    @McpTool(name = "query_order", description = "根据订单号查询电商订单的状态、金额与商品明细")
    public OrderInfo queryOrder(@McpToolParam(description = "订单编号，例如：SO20261001") String orderNo) {
        OrderInfo order = ORDERS.get(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("未找到订单：" + orderNo);
        }
        return order;
    }

    @McpTool(name = "query_logistics", description = "根据订单号查询物流公司与最新配送轨迹")
    public LogisticsInfo queryLogistics(@McpToolParam(description = "订单编号，例如：SO20261001") String orderNo) {
        LogisticsInfo logistics = LOGISTICS.get(orderNo);
        if (logistics == null) {
            throw new IllegalArgumentException("订单 " + orderNo + " 暂无物流信息（可能未发货）");
        }
        return logistics;
    }
}
