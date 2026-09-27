package com.zhoubyte.service.impl;

import com.zhoubyte.pojo.entity.Role;
import com.zhoubyte.mapper.RoleMapper;
import com.zhoubyte.service.RoleService;
import icu.mhb.mybatisplus.plugln.base.service.impl.JoinServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends JoinServiceImpl<RoleMapper, Role> implements RoleService {
}
