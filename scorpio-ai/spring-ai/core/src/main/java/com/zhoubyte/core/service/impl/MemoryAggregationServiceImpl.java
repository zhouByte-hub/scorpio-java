package com.zhoubyte.core.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhoubyte.core.mapper.MemoryAggregationMapper;
import com.zhoubyte.core.pojo.entity.MemoryAggregationEntity;
import com.zhoubyte.core.service.MemoryAggregationService;
import org.springframework.stereotype.Service;

@Service
public class MemoryAggregationServiceImpl
        extends ServiceImpl<MemoryAggregationMapper, MemoryAggregationEntity> implements MemoryAggregationService {
}
