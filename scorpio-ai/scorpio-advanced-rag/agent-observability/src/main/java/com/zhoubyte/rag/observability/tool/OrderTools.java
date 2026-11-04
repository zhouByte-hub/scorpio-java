package com.zhoubyte.rag.observability.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 模拟订单工具组：制造“列表→详情→取消”等嵌套多跳工具调用，便于在 Langfuse 中观察 Span 树。
 */
@Component
public class OrderTools {

    private static final Map<Long, Map<String, Object>> ORDERS = Map.of(
            1001L, Map.of("item", "机械键盘", "amount", 399.0, "status", "已发货", "userId", "u-01"),
            1002L, Map.of("item", "27寸显示器", "amount", 1299.0, "status", "待支付", "userId", "u-01"),
            1003L, Map.of("item", "USB-C 扩展坞", "amount", 199.0, "status", "已签收", "userId", "u-02"));

    private final AtomicLong idGenerator = new AtomicLong(2000);
    private final Map<Long, Map<String, Object>> createdOrders = new ConcurrentHashMap<>();

    @Tool(description = "根据订单号查询订单详情（商品、金额、状态）")
    public Map<String, Object> getOrder(@ToolParam(description = "订单号，如 1001") long orderId) {
        Map<String, Object> order = ORDERS.get(orderId);
        if (order != null) {
            return withId(orderId, order);
        }
        return createdOrders.containsKey(orderId) ? withId(orderId, createdOrders.get(orderId))
                : Map.of("error", "订单不存在: " + orderId);
    }

    @Tool(description = "为指定用户创建新订单，返回订单号")
    public long createOrder(@ToolParam(description = "用户 ID，如 u-01") String userId,
                            @ToolParam(description = "商品名称") String item,
                            @ToolParam(description = "订单金额") double amount) {
        long orderId = idGenerator.incrementAndGet();
        createdOrders.put(orderId, Map.of("item", item, "amount", amount, "status", "待支付", "userId", userId));
        return orderId;
    }

    @Tool(description = "取消待支付状态的订单")
    public String cancelOrder(@ToolParam(description = "订单号") long orderId) {
        Map<String, Object> order = createdOrders.containsKey(orderId)
                ? createdOrders.get(orderId) : ORDERS.get(orderId);
        if (order == null) {
            return "订单不存在: " + orderId;
        }
        if (!"待支付".equals(order.get("status"))) {
            return "订单 %d 状态为 %s，仅待支付订单可取消".formatted(orderId, order.get("status"));
        }
        Map<String, Object> cancelled = new java.util.HashMap<>(order);
        cancelled.put("status", "已取消");
        if (createdOrders.containsKey(orderId)) {
            createdOrders.put(orderId, cancelled);
        }
        return "订单 %d 已取消".formatted(orderId);
    }

    private Map<String, Object> withId(long id, Map<String, Object> order) {
        Map<String, Object> result = new java.util.HashMap<>(order);
        result.put("orderId", id);
        return result;
    }
}
