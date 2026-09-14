package com.zhoubyte.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.mhb.mybatisplus.plugln.annotations.JoinChainModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JoinChainModel
@TableName(value = "msg_aggregation")
public class MemoryAggregationEntity extends BaseEntity{

    @TableField("content")
    private String content;

    @TableField("compression")
    private Integer compression;

    @TableField("last_size")
    private Float lastSize;

    @TableField("current_size")
    private Float currentSize;

}
