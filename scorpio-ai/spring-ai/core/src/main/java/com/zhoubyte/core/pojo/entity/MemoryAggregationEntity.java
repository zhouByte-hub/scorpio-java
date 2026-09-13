package com.zhoubyte.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "msg_aggregation")
public class MemoryAggregationEntity {

    @TableField("content")
    private String content;

    @TableField("compression")
    private Integer compression;

    @TableField("last_size")
    private Float lastSize;

    @TableField("current_size")
    private Float currentSize;

}
