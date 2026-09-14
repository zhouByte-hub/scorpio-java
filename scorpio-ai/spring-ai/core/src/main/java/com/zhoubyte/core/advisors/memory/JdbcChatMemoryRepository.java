package com.zhoubyte.core.advisors.memory;

import com.zhoubyte.core.pojo.entity.ChatMemoryEntity;
import com.zhoubyte.core.pojo.entity.MemoryAggregationEntity;
import com.zhoubyte.core.service.ChatMemoryService;
import com.zhoubyte.core.service.MemoryAggregationService;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMemoryService chatMemoryService;

    public JdbcChatMemoryRepository(ChatMemoryService chatMemoryService) {
        this.chatMemoryService = chatMemoryService;
    }

    @Override
    public List<String> findConversationIds() {
        JoinLambdaWrapper<ChatMemoryEntity> wrapper = new JoinLambdaWrapper<>(ChatMemoryEntity.class);
        wrapper.select(ChatMemoryEntity::getAggregationId)
                .leftJoin(MemoryAggregationEntity.class, MemoryAggregationEntity::getId, ChatMemoryEntity::getAggregationId)
                .distinct()
                .end();
        return chatMemoryService.joinList(wrapper, String.class);
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {

        return List.of();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {

    }

    @Override
    public void deleteByConversationId(String conversationId) {

    }
}
