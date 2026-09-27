package com.zhoubyte.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.mhb.mybatisplus.plugln.annotations.JoinChainModel;
import icu.mhb.mybatisplus.plugln.annotations.TableAlias;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
@JoinChainModel
public class Role {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String code;

    private String name;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
