package com.zhoubyte.service.impl;

import com.zhoubyte.pojo.entity.EmployeeProfile;
import com.zhoubyte.mapper.EmployeeProfileMapper;
import com.zhoubyte.service.EmployeeProfileService;
import icu.mhb.mybatisplus.plugln.base.service.impl.JoinServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmployeeProfileServiceImpl extends JoinServiceImpl<EmployeeProfileMapper, EmployeeProfile>
    implements EmployeeProfileService {
}
