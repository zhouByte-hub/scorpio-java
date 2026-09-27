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

@Data
@TableName("employee")
@JoinChainModel
public class Employee {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    private String name;

    private String jobTitle;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    @JoinField(masterModelClass = Employee.class, masterModelField = "deptId",
            sunModelClass = Dept.class, sunModelField = "id",
            relevancyType = "ONE_TO_ONE")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Dept dept;

    @TableField(exist = false)
    @JoinField(masterModelClass = Employee.class, masterModelField = "id",
            sunModelClass = EmployeeProfile.class, sunModelField = "employeeId",
            relevancyType = "ONE_TO_ONE")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EmployeeProfile profile;
}
