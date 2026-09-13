package com.zhoubyte.core.service.impl;

import com.zhoubyte.core.mapper.MemoryAggregationMapper;
import com.zhoubyte.core.pojo.entity.MemoryAggregationEntity;
import com.zhoubyte.core.service.MemoryAggregationService;
import icu.mhb.mybatisplus.plugln.base.service.impl.JoinServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MemoryAggregationServiceImpl
        extends JoinServiceImpl<MemoryAggregationMapper, MemoryAggregationEntity> implements MemoryAggregationService {
}
