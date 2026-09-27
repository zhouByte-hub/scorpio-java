package com.zhoubyte.mapper;

import com.zhoubyte.pojo.entity.EmployeeProfile;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeProfileMapper extends JoinBaseMapper<EmployeeProfile> {
}
