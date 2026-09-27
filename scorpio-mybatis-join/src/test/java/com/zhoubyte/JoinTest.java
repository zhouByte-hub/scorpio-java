package com.zhoubyte;

import com.zhoubyte.pojo.dto.EmployeeProfileDto;
import com.zhoubyte.pojo.entity.Dept;
import com.zhoubyte.pojo.entity.Employee;
import com.zhoubyte.pojo.vo.EmployeeProfileVo;
import com.zhoubyte.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class JoinTest {

    @Resource
    private ScorpioJoinService scorpioJoinService;

    @Test
    public void multiTableJoinTest(){
        List<EmployeeProfileVo> employeeProfileVos = scorpioJoinService.multiTableJoinTest();
        if(employeeProfileVos.isEmpty()) {
            log.info("没有对应的员工信息");
        }
        employeeProfileVos.forEach(System.out::println);
    }

    @Test
    public void conditionQueryByEntity() {
        Employee employee = new Employee();
        employee.setName("张三");
        Dept dept = new Dept();
        dept.setName("研发部");
        Employee result = scorpioJoinService.queryEmployeeByNotNullEntityTest(employee, dept);
        System.out.println(result);
    }

    @Test
    public void lambdaQuery(){
        List<EmployeeProfileVo> employeeProfileVos = scorpioJoinService.lambdaQueryTest();
        employeeProfileVos.forEach(System.out::println);
    }

    @Test
    public void constructQuery(){
        List<EmployeeProfileVo> employeeProfileVos = scorpioJoinService.constructInvokeJoinTest();
        employeeProfileVos.forEach(System.out::println);
    }

    @Test
    public void autoConditionQuery(){
        EmployeeProfileDto employeeProfileDto = new EmployeeProfileDto();
        employeeProfileDto.setEmployeeName("三");
        List<Employee> employees = scorpioJoinService.autoConditionBuildTest(employeeProfileDto);
        employees.forEach(System.out::println);
    }

    @Test
    public void subQuery(){
        Dept dept = new Dept();
        dept.setName("研发部");
        for (Employee employee : scorpioJoinService.subQueryTest(dept)) {
            System.out.println(employee);
        }
    }

}
