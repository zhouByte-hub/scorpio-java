package com.zhoubyte.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.mhb.mybatisplus.plugln.annotations.JoinChainModel;
import icu.mhb.mybatisplus.plugln.annotations.TableAlias;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("employee_profile")
@JoinChainModel
public class EmployeeProfile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long employeeId;

    private String phone;

    private String email;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
