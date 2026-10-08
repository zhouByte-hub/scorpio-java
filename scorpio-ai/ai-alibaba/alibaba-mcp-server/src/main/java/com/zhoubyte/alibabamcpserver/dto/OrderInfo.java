package com.zhoubyte.alibabamcpserver.dto;

/**
 * 订单信息 DTO：作为 @McpTool 返回值时，框架自动序列化为 JSON 结构化结果。
 *
 * @param orderNo 订单编号
 * @param status  订单状态
 * @param amount  订单金额（元）
 * @param items   商品概要
 */
public record OrderInfo(String orderNo, String status, double amount, String items) {
}
