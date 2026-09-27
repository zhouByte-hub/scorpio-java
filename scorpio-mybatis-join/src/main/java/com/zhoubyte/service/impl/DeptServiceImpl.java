package com.zhoubyte.service.impl;

import com.zhoubyte.pojo.entity.Dept;
import com.zhoubyte.mapper.DeptMapper;
import com.zhoubyte.service.DeptService;
import icu.mhb.mybatisplus.plugln.base.service.impl.JoinServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DeptServiceImpl extends JoinServiceImpl<DeptMapper, Dept> implements DeptService {
}
