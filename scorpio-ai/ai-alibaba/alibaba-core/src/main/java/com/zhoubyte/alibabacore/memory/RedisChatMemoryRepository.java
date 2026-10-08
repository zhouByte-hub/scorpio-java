package com.zhoubyte.alibabacore.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 基于 Redis 的会话记忆存储（spring-ai 1.1.2 未提供官方 Redis 实现，此处自研）。
 *
 * <p>存储结构：
 * <ul>
 *   <li>每个会话一个 List：{@code spring-ai:chat-memory:{conversationId}}，
 *       元素为消息 JSON（{@code {"type":"user","text":"..."}}），按对话顺序 RPUSH；</li>
 *   <li>一个全局 Set：{@code spring-ai:chat-memory:conversations}，记录所有会话 ID，
 *       用于实现 {@link #findConversationIds()}；</li>
 *   <li>会话 List 设置 TTL（默认 7 天），到期自动清理，防止记忆无限堆积。</li>
 * </ul>
 * </p>
 *
 * <p>限制说明（教学取舍）：只持久化消息类型与文本内容，工具调用（ToolCall）、
 * 多模态媒体等元数据不保存；读取时跳过无法还原的消息类型。</p>
 */
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    private static final String KEY_PREFIX = "spring-ai:chat-memory:";
    private static final String CONVERSATIONS_KEY = KEY_PREFIX + "conversations";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisChatMemoryRepository(StringRedisTemplate redisTemplate,
                                     ObjectMapper objectMapper,
                                     Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    @Override
    public List<String> findConversationIds() {
        Set<String> ids = redisTemplate.opsForSet().members(CONVERSATIONS_KEY);
        return ids == null ? List.of() : new ArrayList<>(ids);
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<String> jsonList = redisTemplate.opsForList().range(conversationKey(conversationId), 0, -1);
        if (jsonList == null || jsonList.isEmpty()) {
            return List.of();
        }
        List<Message> messages = new ArrayList<>(jsonList.size());
        for (String json : jsonList) {
            Message message = deserialize(json);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    /**
     * 全量覆盖保存：先删旧 List，再按顺序 RPUSH 新消息，最后刷新 TTL 与会话索引。
     * MessageWindowChatMemory 每次 add 后会带着裁剪过的完整列表调用本方法。
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        String key = conversationKey(conversationId);
        redisTemplate.delete(key);
        if (messages.isEmpty()) {
            redisTemplate.opsForSet().remove(CONVERSATIONS_KEY, conversationId);
            return;
        }
        List<String> jsonList = messages.stream()
                .map(this::serialize)
                .toList();
        redisTemplate.opsForList().rightPushAll(key, jsonList);
        redisTemplate.expire(key, ttl);
        redisTemplate.opsForSet().add(CONVERSATIONS_KEY, conversationId);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        redisTemplate.delete(conversationKey(conversationId));
        redisTemplate.opsForSet().remove(CONVERSATIONS_KEY, conversationId);
    }

    private String conversationKey(String conversationId) {
        return KEY_PREFIX + conversationId;
    }

    /** 消息 -> JSON：仅保留类型与文本两类核心信息 */
    private String serialize(Message message) {
        try {
            return objectMapper.writeValueAsString(new StoredMessage(
                    message.getMessageType().getValue(), message.getText()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("会话消息序列化失败", e);
        }
    }

    /** JSON -> 消息：无法还原的类型（如 tool 响应）返回 null 由调用方跳过 */
    private Message deserialize(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String type = node.path("type").asText();
            String text = node.path("text").asText();
            if (MessageType.USER.getValue().equals(type)) {
                return new UserMessage(text);
            }
            if (MessageType.ASSISTANT.getValue().equals(type)) {
                return new AssistantMessage(text);
            }
            if (MessageType.SYSTEM.getValue().equals(type)) {
                return new SystemMessage(text);
            }
            return null;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("会话消息反序列化失败", e);
        }
    }

    /** Redis 中的消息存储格式 */
    private record StoredMessage(String type, String text) {
    }
}
