package com.zhoubyte.core.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhoubyte.core.mapper.ChatMemoryMapper;
import com.zhoubyte.core.pojo.entity.ChatMemoryEntity;
import com.zhoubyte.core.service.ChatMemoryService;
import org.springframework.stereotype.Service;

@Service
public class ChatMemoryServiceImpl extends ServiceImpl<ChatMemoryMapper, ChatMemoryEntity> implements ChatMemoryService {
}
