package com.zhoubyte.mapper;

import com.zhoubyte.pojo.entity.Dept;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeptMapper extends JoinBaseMapper<Dept> {
}
