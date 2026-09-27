package com.zhoubyte.mapper;

import com.zhoubyte.pojo.entity.Role;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends JoinBaseMapper<Role> {
}
