package com.zhoubyte.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.mhb.mybatisplus.plugln.annotations.JoinChainModel;
import icu.mhb.mybatisplus.plugln.annotations.JoinField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("dept")
@JoinChainModel
public class Dept {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    @JoinField(masterModelClass = Dept.class, masterModelField = "id",
            sunModelClass = Employee.class, sunModelField = "deptId",
            relevancyType = "MANY_TO_MANY")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Employee> employees;
}
