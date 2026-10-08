package com.zhoubyte.alibabamcpserver.dto;

import java.util.List;

/**
 * 物流信息 DTO。
 *
 * @param orderNo    关联订单号
 * @param company    承运快递公司
 * @param trackingNo 运单号
 * @param traces     物流轨迹（按时间倒序）
 */
public record LogisticsInfo(String orderNo, String company, String trackingNo, List<String> traces) {
}
