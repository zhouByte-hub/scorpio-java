package com.zhoubyte.service.impl;

import com.zhoubyte.pojo.dto.EmployeeProfileDto;
import com.zhoubyte.pojo.entity.*;
import com.zhoubyte.pojo.entity.chain.*;
import com.zhoubyte.pojo.vo.EmployeeProfileVo;
import com.zhoubyte.pojo.vo.StatisticsDto;
import com.zhoubyte.service.*;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.extend.Joins;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScorpioJoinServiceImpl implements ScorpioJoinService {

    private final EmployeeService employeeService;

    /**
     * 链式连表。字段方法的参数是 SQL 列别名，要和返回对象的属性名一致。
     * join 的第一个参数是被连进来的表：JOIN 第一张表 ON 第一张表.列 = 第二张表.列。
     */
    @Override
    public List<EmployeeProfileVo> multiTableJoinTest() {
        EmployeeChain employeeChain = EmployeeChain.create();
        EmployeeProfileChain employeeProfileChain = EmployeeProfileChain.create();
        DeptChain deptChain = DeptChain.create();
        RoleChain roleChain = RoleChain.create();
        EmployeeRoleChain employeeRoleChain = EmployeeRoleChain.create();

        return Joins.chain(employeeChain)
                .selectAs(() -> employeeChain.id("employeeId").name("employeeName").jobTitle("jobName")
                        .to(employeeProfileChain)
                        .phone().email()
                        .to(deptChain)
                        .name("deptName")
                        .to(roleChain)
                        .name("roleName").code("roleCode"))
                .innerJoin(employeeProfileChain._employeeId(), employeeChain._id())
                .leftJoin(deptChain._id(), employeeChain._deptId())
                .leftJoin(employeeRoleChain._employeeId(), employeeChain._id())
                .leftJoin(roleChain._id(), employeeRoleChain._roleId())
                .joinList(EmployeeProfileVo.class);
    }

    /**
     * 子查询：dept_id IN (SELECT id FROM dept WHERE name LIKE)。
     * selectSunQuery 只会把子查询放到 SELECT 列上，IN 子查询用 apply 拼到 WHERE。
     */
    @Override
    public List<Employee> subQueryTest(Dept dept) {
        String name = dept.getName() == null ? "" : dept.getName();
        return Joins.of(Employee.class)
                .selectAll()
                .apply("dept_id IN (SELECT id FROM dept WHERE name LIKE {0})", "%" + name + "%")
                .joinList(Employee.class);
    }

    /**
     * 子查询聚合。主表 COUNT 保证没有员工时也只返回一行。
     * 部门数量用 selectSunQuery 放到 SELECT 列上，别名要和 StatisticsDto 的属性名一致。
     * 子查询的字段映射留在子查询 wrapper 上，结果映射只读主表，所以要并回主表。
     * add 的第三个参数为 false，避免把 COUNT(1) 当成字符串加引号。
     */
    @Override
    public StatisticsDto statisticsCount() {
        JoinLambdaWrapper<Employee> wrapper = Joins.of(Employee.class).notDefaultSelectAll();
        wrapper.selectAs(cb -> cb.add("COUNT(1)", "employeeCount", false))
                .selectSunQuery(Dept.class, sub -> {
                    sub.selectAs(cb -> cb.add("COUNT(1)", "deptCount", false));
                    wrapper.getFieldMappingList().addAll(sub.getFieldMappingList());
                });
        return wrapper.joinGetOne(StatisticsDto.class);
    }

    /**
     * 按 DTO 上的条件注解拼接 WHERE。
     * addObjConditions 要写在主表上，注解没写 tableAlias 时用主表别名。
     * leftJoin 返回的是子表 JoinWrapper，没有主表 selectAll 的字段映射，end() 回到主表后再查询。
     */
    @Override
    public List<Employee> autoConditionBuildTest(EmployeeProfileDto employeeProfileDto) {
        return Joins.of(Employee.class)
                .selectAll()
                .addObjConditions(employeeProfileDto)
                .leftJoin(EmployeeProfile.class, EmployeeProfile::getEmployeeId, Employee::getId)
                .end()
                .joinList(Employee.class);
    }

    /**
     * 把实体里的非空字段拼成查询条件。连表时被关联表放在第一个参数。
     */
    @Override
    public Employee queryEmployeeByNotNullEntityTest(Employee employee, Dept dept) {
        EmployeeChain employeeChain = EmployeeChain.create().setEntity(employee);
        DeptChain deptChain = DeptChain.create().setEntity(dept);
        return Joins.chain(employeeChain)
                .initEntityCondition(employeeChain, deptChain)
                .leftJoin(deptChain._id(), employeeChain._deptId())
                .joinGetOne(Employee.class);
    }

    /**
     * Lambda 连表。leftJoin 之后的 selectAs 只作用于当前子表。
     * addFunAlias 的第二个参数是结果对象的属性。end() 结束这一次连表，回到主表。
     */
    @Override
    public List<EmployeeProfileVo> lambdaQueryTest() {
        JoinLambdaWrapper<Employee> joinLambdaWrapper = new JoinLambdaWrapper<>(Employee.class);
        joinLambdaWrapper
                .selectAs(cb -> cb.addFunAlias(Employee::getId, EmployeeProfileVo::getEmployeeId)
                        .addFunAlias(Employee::getName, EmployeeProfileVo::getEmployeeName)
                        .addFunAlias(Employee::getJobTitle, EmployeeProfileVo::getJobName))
                .innerJoin(EmployeeProfile.class, EmployeeProfile::getEmployeeId, Employee::getId)
                .selectAs(cb -> cb.add(EmployeeProfile::getPhone).add(EmployeeProfile::getEmail))
                .end()
                .leftJoin(Dept.class, Dept::getId, Employee::getDeptId)
                .selectAs(cb -> cb.addFunAlias(Dept::getName, EmployeeProfileVo::getDeptName))
                .end()
                .leftJoin(EmployeeRole.class, EmployeeRole::getEmployeeId, Employee::getId)
                .end()
                .leftJoin(Role.class, Role::getId, EmployeeRole::getRoleId)
                .selectAs(cb -> cb.addFunAlias(Role::getName, EmployeeProfileVo::getRoleName)
                        .addFunAlias(Role::getCode, EmployeeProfileVo::getRoleCode))
                .end();
        return employeeService.joinList(joinLambdaWrapper, EmployeeProfileVo.class);
    }

    /**
     * Joins.of 与 new JoinLambdaWrapper 相同，连表写法和 lambdaQueryTest 一致。
     */
    @Override
    public List<EmployeeProfileVo> constructInvokeJoinTest() {
        return Joins.of(Employee.class)
                .selectAs(cb -> cb.addFunAlias(Employee::getId, EmployeeProfileVo::getEmployeeId)
                        .addFunAlias(Employee::getName, EmployeeProfileVo::getEmployeeName)
                        .addFunAlias(Employee::getJobTitle, EmployeeProfileVo::getJobName))
                .innerJoin(EmployeeProfile.class, EmployeeProfile::getEmployeeId, Employee::getId)
                .selectAs(cb -> cb.add(EmployeeProfile::getEmail, EmployeeProfile::getPhone))
                .end()
                .leftJoin(Dept.class, Dept::getId, Employee::getDeptId)
                .selectAs(sb -> sb.addFunAlias(Dept::getName, EmployeeProfileVo::getDeptName))
                .end()
                .leftJoin(EmployeeRole.class, EmployeeRole::getEmployeeId, Employee::getId)
                .end()
                .leftJoin(Role.class, Role::getId, EmployeeRole::getRoleId)
                .selectAs(sb -> sb.addFunAlias(Role::getCode, EmployeeProfileVo::getRoleCode)
                        .addFunAlias(Role::getName, EmployeeProfileVo::getRoleName))
                .end()
                .joinList(EmployeeProfileVo.class);
    }

}
