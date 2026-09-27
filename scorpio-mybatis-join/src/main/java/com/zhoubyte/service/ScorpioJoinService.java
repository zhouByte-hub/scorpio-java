package com.zhoubyte.service;

import com.zhoubyte.pojo.dto.EmployeeProfileDto;
import com.zhoubyte.pojo.entity.Dept;
import com.zhoubyte.pojo.entity.Employee;
import com.zhoubyte.pojo.vo.EmployeeProfileVo;
import com.zhoubyte.pojo.vo.StatisticsDto;

import java.util.List;

public interface ScorpioJoinService {

    List<EmployeeProfileVo> multiTableJoinTest();

    List<Employee> subQueryTest(Dept dept);

    List<Employee> autoConditionBuildTest(EmployeeProfileDto employeeProfileDto);

    Employee queryEmployeeByNotNullEntityTest(Employee employee, Dept dept);

    List<EmployeeProfileVo> lambdaQueryTest();

    List<EmployeeProfileVo> constructInvokeJoinTest();

    StatisticsDto statisticsCount();

}
