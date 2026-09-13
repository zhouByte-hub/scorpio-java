package com.zhoubyte.core.advisors.memory;

import com.zhoubyte.core.service.ChatMemoryService;
import com.zhoubyte.core.service.MemoryAggregationService;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMemoryService chatMemoryService;
    private final MemoryAggregationService memoryAggregationService;

    public JdbcChatMemoryRepository(ChatMemoryService chatMemoryService, MemoryAggregationService memoryAggregationService) {
        this.chatMemoryService = chatMemoryService;
        this.memoryAggregationService = memoryAggregationService;
    }

    @Override
    public List<String> findConversationIds() {

        return List.of();
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
