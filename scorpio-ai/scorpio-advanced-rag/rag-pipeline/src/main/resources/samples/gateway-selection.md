---
title: API 网关选型技术方案
department: 研发部
level: internal
docType: 技术方案
docDate: 2026-03-12
---

# API 网关选型技术方案

## 背景

随着微服务数量增长到 60 余个，散落在各服务的鉴权、限流、灰度逻辑重复建设严重。本方案对 Spring Cloud Gateway、Higress、APISIX 三款网关进行选型评估，最终建议统一接入层。

## 结论

选型结论：选用 Spring Cloud Gateway 作为统一 API 网关。理由如下：

1. 团队现有技术栈为 Spring Boot，运维心智成本低，与 Nacos 注册中心天然集成；
2. 基准测试中单节点 4C8G 可承载 1.2 万 QPS，满足当前峰值 3000 QPS 并留有 4 倍冗余；
3. 插件体系支持 Java 扩展，业务团队可自助开发过滤器。

APISIX 性能最优（单节点 3 万 QPS）但需要 etcd 与 Lua 技术栈，维护成本高，作为二期备选。Higress 因社区版本迭代快、API 变动频繁，暂不采用。

## 实施要求

网关接入后，所有对外接口必须启用 OAuth2 鉴权过滤器；内部服务间调用走网关白名单。限流规则统一配置在 Nacos 的 gateway-rules 分组，集群生效延迟不超过 10 秒。本项目立项编号 GW-2026-018，预算 12 万元，由架构组负责实施，预计 2026 年 6 月 30 日前完成全量接入。
