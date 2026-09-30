package com.zhoubyte.core.advisors.memory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhoubyte.core.mapper.ChatMemoryMapper;
import com.zhoubyte.core.mapper.MemoryAggregationMapper;
import com.zhoubyte.core.pojo.entity.ChatMemoryEntity;
import com.zhoubyte.core.pojo.entity.MemoryAggregationEntity;
import com.zhoubyte.core.pojo.entity.chain.ChatMemoryEntityChain;
import com.zhoubyte.core.pojo.entity.chain.MemoryAggregationEntityChain;
import icu.mhb.mybatisplus.plugln.extend.Joins;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class JdbcChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMemoryMapper chatMemoryMapper;
    private final MemoryAggregationMapper memoryAggregationMapper;

    public JdbcChatMemoryRepository(ChatMemoryMapper chatMemoryMapper, MemoryAggregationMapper memoryAggregationMapper) {
        this.chatMemoryMapper = chatMemoryMapper;
        this.memoryAggregationMapper = memoryAggregationMapper;
    }

    @Override
    public List<String> findConversationIds() {
        ChatMemoryEntityChain chatMemoryChain = ChatMemoryEntityChain.create();
        MemoryAggregationEntityChain memoryAggregationChain = MemoryAggregationEntityChain.create();
        return Joins.chain(chatMemoryChain)
                .selectAs(chatMemoryChain::id)
                .innerJoin(memoryAggregationChain._id(), chatMemoryChain._aggregationId())
                .joinList(String.class);
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        MemoryAggregationEntity memoryAggregationEntity = memoryAggregationMapper.selectById(conversationId);
        return memoryAggregationEntity.getContent().stream()
                .filter(Objects::nonNull)
                .map(msg -> {
                    Message message = null;
                    if (MessageType.SYSTEM.getValue().equals(msg.type()) || MessageType.TOOL.getValue().equals(msg.type())) {
                        message = new SystemMessage(msg.content());
                    }
                    if (MessageType.USER.getValue().equals(msg.type())) {
                        message = new UserMessage(msg.content());
                    }
                    return message;
                }).toList();
    }

    /**
     * 覆盖写入该会话的全部消息。聚合主键使用会话 ID，和按主键查询的读取方式一致。
     * message、content 是 JSON 列，依赖 JacksonTypeHandler 写入合法 JSON。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
        List<Message> savedMessages = messages == null
                ? List.of()
                : messages.stream().filter(Objects::nonNull).toList();
        chatMemoryMapper.delete(Wrappers.<ChatMemoryEntity>lambdaUpdate()
                .eq(ChatMemoryEntity::getConversationId, conversationId));
        if (savedMessages.isEmpty()) {
            memoryAggregationMapper.deleteById(conversationId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<MemoryAggregationEntity.AggregationContent> contents = savedMessages.stream()
                .map(message -> new MemoryAggregationEntity.AggregationContent(
                        message.getMessageType().getValue(), message.getText()))
                .toList();
        float currentSize = savedMessages.stream()
                .map(Message::getText)
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .sum();
        MemoryAggregationEntity aggregation = memoryAggregationMapper.selectById(conversationId);
        if (aggregation == null) {
            aggregation = new MemoryAggregationEntity();
            aggregation.setId(conversationId);
            aggregation.setContent(contents);
            aggregation.setCompression(0);
            aggregation.setLastSize(0F);
            aggregation.setCurrentSize(currentSize);
            aggregation.setCreateTime(now);
            aggregation.setUpdateTime(now);
            memoryAggregationMapper.insert(aggregation);
        } else {
            aggregation.setLastSize(aggregation.getCurrentSize() == null ? 0F : aggregation.getCurrentSize());
            aggregation.setContent(contents);
            aggregation.setCurrentSize(currentSize);
            aggregation.setUpdateTime(now);
            memoryAggregationMapper.updateById(aggregation);
        }

        for (Message message : savedMessages) {
            ChatMemoryEntity entity = new ChatMemoryEntity();
            entity.setConversationId(conversationId);
            entity.setAggregationId(conversationId);
            entity.setMessageType(message.getMessageType().getValue());
            entity.setMessage(message.getText());
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            chatMemoryMapper.insert(entity);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByConversationId(String conversationId) {
        String aggregationId = Joins.of(ChatMemoryEntity.class)
                .notDefaultSelectAll()
                .eq(ChatMemoryEntity::getConversationId, conversationId)
                .innerJoin(MemoryAggregationEntity.class, MemoryAggregationEntity::getId, ChatMemoryEntity::getAggregationId)
                .selectAs(cb -> cb.add(MemoryAggregationEntity::getId))
                .end()
                .joinGetOne(String.class);
        int chatDeleteResult = chatMemoryMapper.delete(Wrappers.<ChatMemoryEntity>lambdaUpdate()
                .eq(ChatMemoryEntity::getConversationId, conversationId));
        if (chatDeleteResult <= 0) {
            throw new RuntimeException("删除会话失败");
        }
        if (StringUtils.isBlank(aggregationId)) {
            log.error("{}暂无会话压缩", conversationId);
            return;
        }
        if (memoryAggregationMapper.deleteById(aggregationId) <= 0) {
            throw new RuntimeException("删除会话压缩信息失败");
        }
    }
}
