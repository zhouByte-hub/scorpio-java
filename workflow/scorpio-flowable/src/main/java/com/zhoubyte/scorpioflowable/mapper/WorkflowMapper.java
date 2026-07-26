package com.zhoubyte.scorpioflowable.mapper;

import com.zhoubyte.scorpioflowable.response.WorkFlowResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作流Mapper接口
 */
@Mapper
public interface WorkflowMapper {

    /**
     * 查询当前用户的活跃工作流
     * @param userId 用户ID
     * @return 工作流响应列表
     */
    List<WorkFlowResponse> currentUserWorkflow(@Param("userId") String userId);
}
