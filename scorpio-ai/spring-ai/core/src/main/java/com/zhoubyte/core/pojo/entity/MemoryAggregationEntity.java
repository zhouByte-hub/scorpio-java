package com.zhoubyte.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import icu.mhb.mybatisplus.plugln.annotations.JoinChainModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JoinChainModel
@TableName(value = "msg_aggregation", autoResultMap = true)
public class MemoryAggregationEntity extends BaseEntity{

    @TableField(value = "content", typeHandler = JacksonTypeHandler.class)
    private List<AggregationContent> content;

    @TableField("compression")
    private Integer compression;

    @TableField("last_size")
    private Float lastSize;

    @TableField("current_size")
    private Float currentSize;


    public record AggregationContent(String type, String content) {
    }

}
