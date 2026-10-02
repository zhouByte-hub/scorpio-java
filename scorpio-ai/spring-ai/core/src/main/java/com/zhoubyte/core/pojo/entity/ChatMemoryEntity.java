package com.zhoubyte.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import icu.mhb.mybatisplus.plugln.annotations.JoinChainModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.ai.chat.messages.Message;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "chat_memory", autoResultMap = true)
@Data
@JoinChainModel
public class ChatMemoryEntity extends BaseEntity{

    @TableField("message_type")
    private String messageType;

    @TableField(value = "message", typeHandler = Fastjson2TypeHandler.class)
    private String message;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("user_id")
    private String userId;

    @TableField("username")
    private String username;

    @TableField("aggregation_id")
    private String aggregationId;


    public static ChatMemoryEntity of(Message message, String aggregationId, String conversationId) {
        ChatMemoryEntity chatMemoryEntity = new ChatMemoryEntity();
        chatMemoryEntity.setAggregationId(aggregationId);
        chatMemoryEntity.setConversationId(conversationId);
        chatMemoryEntity.setMessage(message.getText());
        chatMemoryEntity.setMessageType(message.getMessageType().getValue());
        return chatMemoryEntity;
    }


}
