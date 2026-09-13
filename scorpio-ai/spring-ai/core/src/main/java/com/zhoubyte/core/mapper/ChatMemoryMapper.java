package com.zhoubyte.core.mapper;

import com.zhoubyte.core.pojo.entity.ChatMemoryEntity;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMemoryMapper extends JoinBaseMapper<ChatMemoryEntity> {
}
