package com.zhoubyte.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeProfileVo {

    private String employeeId;          // 员工ID
    private String employeeName;        // 员工名称
    private String jobName;             // 岗位
    private String phone;               // 手机号
    private String email;               // 邮箱
    private String deptName;            // 部门
    private String roleCode;            // 角色编号
    private String roleName;            // 角色名称

}
