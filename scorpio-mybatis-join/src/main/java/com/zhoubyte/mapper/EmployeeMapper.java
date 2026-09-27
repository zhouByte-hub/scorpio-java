package com.zhoubyte.mapper;

import com.zhoubyte.pojo.entity.Employee;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends JoinBaseMapper<Employee> {
}
