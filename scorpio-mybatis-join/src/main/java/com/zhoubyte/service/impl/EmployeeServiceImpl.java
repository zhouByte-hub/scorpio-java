package com.zhoubyte.service.impl;

import com.zhoubyte.pojo.entity.Employee;
import com.zhoubyte.mapper.EmployeeMapper;
import com.zhoubyte.service.EmployeeService;
import icu.mhb.mybatisplus.plugln.base.service.impl.JoinServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends JoinServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
