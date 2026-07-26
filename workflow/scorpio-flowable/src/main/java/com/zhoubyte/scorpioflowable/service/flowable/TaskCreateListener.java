package com.zhoubyte.scorpioflowable.service.flowable;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在BPMN的任务节点中将监听器的全类名写在"执行监听器"部分。
 */
public class TaskCreateListener implements TaskListener {

    private static final Logger log = LoggerFactory.getLogger(TaskCreateListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        // 设置后选组
        delegateTask.addCandidateGroup("XSB");
        // 设置候选人
        delegateTask.addCandidateUser("userId");
        if(EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            log.info("Task create");
        }
    }
}
