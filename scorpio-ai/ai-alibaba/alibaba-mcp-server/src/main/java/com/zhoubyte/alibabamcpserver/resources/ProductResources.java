package com.zhoubyte.alibabamcpserver.resources;

import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 电商商品 MCP Resource。
 *
 * <p>Resource 是「只读数据」的暴露方式：uri 模板中的 {productId} 是变量，
 * 客户端请求 product://1001 时，框架把 1001 注入方法参数并返回文本内容。</p>
 */
@Component
public class ProductResources {

    private static final Map<String, String> PRODUCTS = Map.of(
            "1001", "无线蓝牙耳机：主动降噪、续航 30 小时、蓝牙 5.4，售价 299 元。",
            "1002", "机械键盘：Gasket 结构、三模连接、98 键布局，售价 499 元。",
            "1003", "手机壳：磁吸 MagSafe 兼容、防摔气囊设计，售价 39 元。");

    @McpResource(
            uri = "product://{productId}",
            name = "product-detail",
            title = "商品详情",
            description = "按商品 ID 读取商品详情介绍",
            mimeType = "text/plain")
    public String loadProduct(@McpArg(name = "productId", description = "商品 ID，例如：1001", required = true)
                              String productId) {
        return PRODUCTS.getOrDefault(productId, "商品 " + productId + " 不存在");
    }
}
