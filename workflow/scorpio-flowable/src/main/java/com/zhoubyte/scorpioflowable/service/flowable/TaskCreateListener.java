package com.zhoubyte.scorpioflowable.service.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * 在BPMN的任务节点中将监听器的全类名写在“执行监听器”部分。
 */
@Slf4j
public class TaskCreateListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        // 设置后选组
        delegateTask.addCandidateGroup("XSB");
        if(EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            log.info("Task create");
        }
    }
}
