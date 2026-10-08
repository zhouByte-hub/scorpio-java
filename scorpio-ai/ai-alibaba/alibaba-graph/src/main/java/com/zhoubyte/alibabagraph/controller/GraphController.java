package com.zhoubyte.alibabagraph.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Graph 工作流演示接口：顺序编排、条件路由、流式执行。
 */
@RestController
@RequestMapping("/graph")
public class GraphController {

    private final CompiledGraph articleGraph;
    private final CompiledGraph customerServiceGraph;

    public GraphController(CompiledGraph articleGraph, CompiledGraph customerServiceGraph) {
        this.articleGraph = articleGraph;
        this.customerServiceGraph = customerServiceGraph;
    }

    /**
     * 顺序工作流：invoke() 同步执行整张图，返回最终状态。
     * 示例：/graph/article?topic=人工智能如何改变教育
     */
    @GetMapping("/article")
    public Map<String, Object> article(@RequestParam String topic) {
        Optional<OverAllState> result = articleGraph.invoke(Map.of("topic", topic));
        Map<String, Object> response = new LinkedHashMap<>();
        result.ifPresent(state -> {
            response.put("topic", state.value("topic").orElse(null));
            response.put("outline", state.value("outline").orElse(null));
            response.put("article", state.value("article").orElse(null));
        });
        return response;
    }

    /**
     * 条件路由：先由模型识别意图，再路由到对应专员节点。
     * 示例：/graph/service?question=我买的耳机怎么退货
     */
    @GetMapping("/service")
    public Map<String, Object> service(@RequestParam String question) {
        Optional<OverAllState> result = customerServiceGraph.invoke(Map.of("question", question));
        Map<String, Object> response = new LinkedHashMap<>();
        result.ifPresent(state -> {
            response.put("question", state.value("question").orElse(null));
            response.put("intent", state.value("intent").orElse(null));
            response.put("answer", state.value("answer").orElse(null));
        });
        return response;
    }

    /**
     * 流式执行：stream() 每完成一个节点就推送一次 NodeOutput（SSE），
     * 前端可实时展示各阶段的中间产物，而不是等待整条链路结束。
     * 示例：curl -N "http://localhost:8091/alibaba-graph/graph/stream?topic=春天的杭州"
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> stream(@RequestParam String topic) {
        return articleGraph.stream(Map.of("topic", topic))
                .map(GraphController::formatNodeOutput)
                .filter(text -> !text.isEmpty());
    }

    /** 把每个节点的输出整理成可读文本；START / END 虚拟节点不输出 */
    private static String formatNodeOutput(NodeOutput output) {
        if (output instanceof StreamingOutput<?> streaming) {
            // chunk() 已弃用，改用 message() 获取增量文本
            String text = streaming.message() == null ? "" : streaming.message().getText();
            return "[" + streaming.node() + "] " + text;
        }
        if (output.isSTART() || output.isEND()) {
            return "";
        }
        OverAllState state = output.state();
        Object value = state.value("outline").or(() -> state.value("article")).orElse(null);
        return "[" + output.node() + " 完成] " + value;
    }
}
