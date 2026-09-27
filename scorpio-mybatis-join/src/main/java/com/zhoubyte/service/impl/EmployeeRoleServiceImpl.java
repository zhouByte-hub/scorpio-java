package com.zhoubyte.service.impl;

import com.zhoubyte.pojo.entity.EmployeeRole;
import com.zhoubyte.mapper.EmployeeRoleMapper;
import com.zhoubyte.service.EmployeeRoleService;
import icu.mhb.mybatisplus.plugln.base.service.impl.JoinServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmployeeRoleServiceImpl extends JoinServiceImpl<EmployeeRoleMapper, EmployeeRole> implements EmployeeRoleService{

}
