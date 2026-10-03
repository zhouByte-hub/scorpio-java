package com.zhoubyte.core.advisors.memory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhoubyte.core.config.ScorpioConfig;
import com.zhoubyte.core.mapper.ChatMemoryMapper;
import com.zhoubyte.core.mapper.MemoryAggregationMapper;
import com.zhoubyte.core.pojo.entity.ChatMemoryEntity;
import com.zhoubyte.core.pojo.entity.MemoryAggregationEntity;
import com.zhoubyte.core.pojo.entity.chain.ChatMemoryEntityChain;
import com.zhoubyte.core.pojo.entity.chain.MemoryAggregationEntityChain;
import icu.mhb.mybatisplus.plugln.extend.Joins;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class JdbcChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMemoryMapper chatMemoryMapper;
    private final MemoryAggregationMapper memoryAggregationMapper;
    private final ScorpioConfig scorpioConfig;

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
        if(memoryAggregationEntity == null) {
            return List.of();
        }
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
    public void saveAll(@NonNull String conversationId, @NonNull List<Message> messages) {
        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
        if(messages == null || messages.isEmpty()) {
            return;
        }
        MemoryAggregationEntity memoryAggregationEntity = Joins.of(ChatMemoryEntity.class)
                .innerJoin(MemoryAggregationEntity.class, MemoryAggregationEntity::getId, ChatMemoryEntity::getAggregationId)
                .selectAll()
                .end()
                .joinGetOne(MemoryAggregationEntity.class);

        List<MemoryAggregationEntity.AggregationContent> content = null;
        boolean isUpdate = false;
        if(memoryAggregationEntity == null) {
            memoryAggregationEntity = new MemoryAggregationEntity();
            content = new LinkedList<>();
        }else {
            content = memoryAggregationEntity.getContent();
            isUpdate = true;
        }
        List<ChatMemoryEntity> chatMemoryEntities = new LinkedList<>();
        for (Message message : messages) {
            MemoryAggregationEntity.AggregationContent aggregationContent
                    = new MemoryAggregationEntity.AggregationContent(message.getMessageType().getValue(), message.getText());
            content.add(aggregationContent);
            chatMemoryEntities.add(ChatMemoryEntity.of(message, memoryAggregationEntity.getId(), conversationId));
        }
        memoryAggregationEntity.setContent(content);
        // todo 计算content大小进行压缩
//        if(contentSize(content) >= scorpioConfig.getMemorySize()) {
//
//        }
        String aggId = memoryAggregationEntity.getId();
        if(isUpdate) {
            memoryAggregationMapper.updateById(memoryAggregationEntity);
        }else{
            memoryAggregationMapper.insert(memoryAggregationEntity);
            aggId = memoryAggregationEntity.getId();
        }
        for (ChatMemoryEntity chatMemoryEntity : chatMemoryEntities) {
            chatMemoryEntity.setAggregationId(aggId);
            chatMemoryMapper.insert(chatMemoryEntity);
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

    private float contentSize(List<MemoryAggregationEntity.AggregationContent> content) {
        if (content == null || content.isEmpty()) {
            return 0F;
        }
        int bytes = 0;
        for (MemoryAggregationEntity.AggregationContent item : content) {
            if (item == null || item.content() == null) {
                continue;
            }
            bytes += item.content().getBytes(StandardCharsets.UTF_8).length;
        }
        return bytes;
    }
}
