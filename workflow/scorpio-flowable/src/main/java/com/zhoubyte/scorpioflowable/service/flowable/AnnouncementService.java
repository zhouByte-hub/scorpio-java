package com.zhoubyte.scorpioflowable.service.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * 公司公告服务任务实现类
 *
 * <p>为什么需要这个类：
 * <ul>
 *   <li>在 BPMN 流程中，serviceTask（服务任务）用于执行自动化的业务逻辑</li>
 *   <li>与 userTask 不同，serviceTask 不需要人工干预，由系统自动执行</li>
 *   <li>Flowable 要求 serviceTask 必须指定实现方式，否则部署时会报错</li>
 * </ul>
 *
 * <p>类的作用：
 * <ul>
 *   <li>实现 {@link JavaDelegate} 接口，作为 Flowable 服务任务的执行入口</li>
 *   <li>在流程执行到"公司公告"节点时，自动触发此类执行</li>
 *   <li>可用于发送公告通知、记录日志、调用外部系统等自动化操作</li>
 * </ul>
 *
 * <p>Flowable 调用机制：
 * <ol>
 *   <li>BPMN XML 中的 serviceTask 通过 {@code flowable:class} 属性指定全限定类名</li>
 *   <li>当流程执行到该 serviceTask 节点时，Flowable 引擎会通过反射实例化此类</li>
 *   <li>引擎调用 {@link #execute(DelegateExecution)} 方法，传入当前执行上下文</li>
 *   <li>通过 {@link DelegateExecution} 可以获取流程变量、设置变量、获取流程实例信息等</li>
 * </ol>
 *
 * <p>BPMN 配置示例：
 * <pre>{@code
 * <serviceTask id="announcement" name="公司公告"
 *              flowable:class="com.zhoubyte.scorpioflowable.service.flowable.AnnouncementService">
 * </serviceTask>
 * }</pre>
 *
 * @see JavaDelegate
 * @see DelegateExecution
 */
@Slf4j
public class AnnouncementService implements JavaDelegate {

    /**
     * 执行公司公告服务任务
     *
     * <p>当流程执行到此 serviceTask 时，Flowable 引擎会自动调用此方法。
     *
     * @param execution 执行上下文，包含流程实例信息、流程变量等
     *                  可以通过 {@code execution.getVariable("varName")} 获取流程变量
     *                  可以通过 {@code execution.setVariable("varName", value)} 设置流程变量
     */
    @Override
    public void execute(DelegateExecution execution) {
        log.info("执行公司公告服务任务");
    }
}