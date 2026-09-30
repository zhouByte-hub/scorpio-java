package com.zhoubyte.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import icu.mhb.mybatisplus.plugln.annotations.JoinChainModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "chat_memory", autoResultMap = true)
@Data
@JoinChainModel
public class ChatMemoryEntity extends BaseEntity{

    @TableField("message_type")
    private String messageType;

    @TableField(value = "message", typeHandler = JacksonTypeHandler.class)
    private String message;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("user_id")
    private String userId;

    @TableField("username")
    private String username;

    @TableField("aggregation_id")
    private String aggregationId;


}
