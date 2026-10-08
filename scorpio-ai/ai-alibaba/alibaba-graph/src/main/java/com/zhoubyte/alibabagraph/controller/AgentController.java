package com.zhoubyte.alibabagraph.controller;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * ReactAgent 演示接口：模型自主决定何时调用 CalculatorTools 完成计算任务。
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    private final ReactAgent calculatorAgent;

    public AgentController(ReactAgent calculatorAgent) {
        this.calculatorAgent = calculatorAgent;
    }

    /**
     * 同步调用 Agent，内部自动执行 ReAct 循环（思考 → 工具调用 → 观察 → 回答）。
     * 示例：/agent/chat?question=128 乘以 7 再减去 16 等于多少
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String question) throws GraphRunnerException {
        AssistantMessage answer = calculatorAgent.call(question);
        return answer.getText();
    }

    /**
     * 流式调用 Agent：streamMessages 逐条推送中间消息与最终回答（SSE）。
     * 示例：curl -N "http://localhost:8091/alibaba-graph/agent/stream?question=计算 3.5 加 4.5"
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> stream(@RequestParam String question) throws GraphRunnerException {
        return calculatorAgent.streamMessages(question)
                .map(Message::getText);
    }
}
