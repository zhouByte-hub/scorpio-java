package com.zhoubyte.core.service.impl;

import com.zhoubyte.core.mapper.ChatMemoryMapper;
import com.zhoubyte.core.pojo.entity.ChatMemoryEntity;
import com.zhoubyte.core.service.ChatMemoryService;
import icu.mhb.mybatisplus.plugln.base.service.impl.JoinServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ChatMemoryServiceImpl
        extends JoinServiceImpl<ChatMemoryMapper, ChatMemoryEntity> implements ChatMemoryService {
}
