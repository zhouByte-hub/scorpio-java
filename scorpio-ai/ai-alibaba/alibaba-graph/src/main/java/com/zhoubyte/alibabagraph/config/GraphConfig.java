package com.zhoubyte.alibabagraph.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.zhoubyte.alibabagraph.tool.CalculatorTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Spring AI Alibaba Graph 工作流装配。
 *
 * <p>核心概念：
 * <ul>
 *   <li>{@link StateGraph}：图的定义（节点 + 边），编译后得到 {@link CompiledGraph}；</li>
 *   <li>OverAllState：全局共享状态，节点返回的 Map 会按 KeyStrategy 合并进状态
 *       （ReplaceStrategy 覆盖、AppendStrategy 追加）；</li>
 *   <li>节点用 {@link AsyncNodeAction#node_async} 包装同步 NodeAction，
 *       条件路由用 {@link AsyncEdgeAction#edge_async} 包装 EdgeAction；</li>
 *   <li>START / END 是框架预置的虚拟起止节点。</li>
 * </ul>
 * </p>
 */
@Configuration
public class GraphConfig {

    /**
     * 供图节点内部调用模型使用的 ChatClient（无记忆，单次请求）。
     */
    @Bean
    public ChatClient graphChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * 示例一：顺序工作流（串行 Pipeline）。
     * START → 生成提纲 → 撰写文章 → END，前一个节点的输出作为后一个节点的输入。
     */
    @Bean
    public CompiledGraph articleGraph(ChatClient graphChatClient) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
                .addStrategy("topic", new ReplaceStrategy())
                .addStrategy("outline", new ReplaceStrategy())
                .addStrategy("article", new ReplaceStrategy())
                .build();

        StateGraph graph = new StateGraph(keyStrategyFactory);

        // 节点 1：根据主题生成提纲
        graph.addNode("outline", AsyncNodeAction.node_async(state -> {
            String topic = state.value("topic", "");
            String outline = graphChatClient.prompt()
                    .system("你是资深写作教练，请为给定主题列出 3 条提纲要点，只输出要点列表。")
                    .user("主题：" + topic)
                    .call()
                    .content();
            return Map.of("outline", outline);
        }));

        // 节点 2：根据主题 + 提纲撰写完整文章
        graph.addNode("article", AsyncNodeAction.node_async(state -> {
            String topic = state.value("topic", "");
            String outline = state.value("outline", "");
            String article = graphChatClient.prompt()
                    .system("你是专业专栏作者，请依据提纲写出一篇 300 字左右的短文。")
                    .user("主题：" + topic + "\n提纲：\n" + outline)
                    .call()
                    .content();
            return Map.of("article", article);
        }));

        graph.addEdge(StateGraph.START, "outline");
        graph.addEdge("outline", "article");
        graph.addEdge("article", StateGraph.END);

        return graph.compile();
    }

    /**
     * 示例二：条件路由工作流（智能客服分流）。
     * START → 意图识别 →（售后 / 技术 / 咨询）→ 对应处理节点 → END。
     * addConditionalEdges 的 Map：key 为路由函数返回值，value 为目标节点名。
     */
    @Bean
    public CompiledGraph customerServiceGraph(ChatClient graphChatClient) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
                .addStrategy("question", new ReplaceStrategy())
                .addStrategy("intent", new ReplaceStrategy())
                .addStrategy("answer", new ReplaceStrategy())
                .build();

        StateGraph graph = new StateGraph(keyStrategyFactory);

        // 路由节点：让模型把问题分类为 售后 / 技术 / 咨询
        graph.addNode("classify", AsyncNodeAction.node_async(state -> {
            String question = state.value("question", "");
            String raw = graphChatClient.prompt()
                    .system("判断用户问题属于哪一类，只输出一个词：售后、技术 或 咨询，禁止解释。")
                    .user(question)
                    .call()
                    .content();
            String intent = normalizeIntent(raw);
            return Map.of("intent", intent);
        }));

        // 售后处理节点
        graph.addNode("after_sale", AsyncNodeAction.node_async(state -> Map.of(
                "answer", "【售后专员】" + graphChatClient.prompt()
                        .system("你是电商售后专员，处理退换货、物流、退款问题，回答简洁专业。")
                        .user(state.value("question", ""))
                        .call().content())));

        // 技术支持节点
        graph.addNode("tech_support", AsyncNodeAction.node_async(state -> Map.of(
                "answer", "【技术支持】" + graphChatClient.prompt()
                        .system("你是产品技术支持工程师，解答使用与故障排查问题，给出分步建议。")
                        .user(state.value("question", ""))
                        .call().content())));

        // 一般咨询节点
        graph.addNode("general_consult", AsyncNodeAction.node_async(state -> Map.of(
                "answer", "【客服代表】" + graphChatClient.prompt()
                        .system("你是热情的客服代表，解答商品与活动咨询。")
                        .user(state.value("question", ""))
                        .call().content())));

        graph.addEdge(StateGraph.START, "classify");
        graph.addConditionalEdges("classify",
                AsyncEdgeAction.edge_async(state -> state.value("intent", "咨询")),
                Map.of("售后", "after_sale", "技术", "tech_support", "咨询", "general_consult"));
        graph.addEdge("after_sale", StateGraph.END);
        graph.addEdge("tech_support", StateGraph.END);
        graph.addEdge("general_consult", StateGraph.END);

        return graph.compile();
    }

    /**
     * 示例三：ReactAgent（推理 + 行动循环）。
     * Agent 自动完成「模型思考 → 调用工具 → 观察结果 → 继续生成」的 ReAct 闭环，
     * methodTools 会把对象上所有 @Tool 方法注册为可用工具。
     */
    @Bean
    public ReactAgent calculatorAgent(ChatModel chatModel, CalculatorTools calculatorTools)
            throws GraphStateException {
        return ReactAgent.builder()
                .name("calculator-agent")
                .model(chatModel)
                .instruction("你是数学助手“小算盘”，涉及计算时必须调用计算器工具，再给出答案。")
                .methodTools(calculatorTools)
                .outputKey("answer")
                .build();
    }

    /** 兜底归一化模型输出的意图，防止小模型输出多余字符导致路由失败 */
    private static String normalizeIntent(String raw) {
        if (raw == null) {
            return "咨询";
        }
        if (raw.contains("售后")) {
            return "售后";
        }
        if (raw.contains("技术")) {
            return "技术";
        }
        return "咨询";
    }
}
