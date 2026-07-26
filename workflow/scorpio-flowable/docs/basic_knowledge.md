# Flowable 核心表结构说明

本文档按照功能模块分类介绍Flowable工作流引擎的核心数据库表。所有表结构均从实际数据库中提取，并为每个字段提供详细说明。

---

# 1. 流程历史记录表

流程历史记录表用于记录流程执行过程中的历史信息，包括流程实例、任务、活动、变量等的执行记录。这些表中的数据在流程运行期间和结束后都会保留，用于流程追溯、审计和统计分析。

## （1）ACT_HI_ACTINST - 历史活动实例表

**表作用**：记录流程中每个活动节点（包括用户任务、服务任务、网关、开始/结束事件等）的执行历史，包括开始时间、结束时间、执行时长、办理人等信息。

### 表结构

```sql
CREATE TABLE `ACT_HI_ACTINST` (
  `ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_DEF_ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8mb3_bin NOT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `CALL_PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_NAME_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_TYPE_` varchar(255) COLLATE utf8mb3_bin NOT NULL,
  `ASSIGNEE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `COMPLETED_BY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `TRANSACTION_ORDER_` int DEFAULT NULL,
  `DURATION_` bigint DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_ACT_INST_START` (`START_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_PROCINST` (`PROC_INST_ID_`,`ACT_ID_`),
  KEY `ACT_IDX_HI_ACT_INST_EXEC` (`EXECUTION_ID_`,`ACT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
```

### 字段说明

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| ID_ | varchar(64) | 是 | 主键ID，活动实例的唯一标识 |
| REV_ | int | 否 | 版本号，用于乐观锁控制，每次更新加1 |
| PROC_DEF_ID_ | varchar(64) | 是 | 流程定义ID，关联ACT_RE_PROCDEF表 |
| PROC_INST_ID_ | varchar(64) | 是 | 流程实例ID，标识该活动所属的流程实例 |
| EXECUTION_ID_ | varchar(64) | 是 | 执行实例ID，标识该活动所属的执行路径 |
| ACT_ID_ | varchar(255) | 是 | 活动ID，BPMN流程定义中的活动节点ID |
| TASK_ID_ | varchar(64) | 否 | 任务ID，如果是用户任务，关联ACT_HI_TASKINST表 |
| CALL_PROC_INST_ID_ | varchar(64) | 否 | 调用的流程实例ID，用于调用活动(Call Activity) |
| ACT_NAME_ | varchar(255) | 否 | 活动名称，BPMN中定义的活动显示名称 |
| ACT_TYPE_ | varchar(255) | 是 | 活动类型，如：startEvent、userTask、serviceTask、exclusiveGateway等 |
| ASSIGNEE_ | varchar(255) | 否 | 办理人，用户任务的当前办理人 |
| COMPLETED_BY_ | varchar(255) | 否 | 完成人，完成任务的用户ID |
| START_TIME_ | datetime(3) | 是 | 开始时间，活动开始执行的精确时间 |
| END_TIME_ | datetime(3) | 否 | 结束时间，活动执行完成的精确时间 |
| TRANSACTION_ORDER_ | int | 否 | 事务顺序，同一事务中的操作顺序 |
| DURATION_ | bigint | 否 | 持续时间，活动执行的毫秒数（END_TIME_ - START_TIME_） |
| DELETE_REASON_ | varchar(4000) | 否 | 删除原因，活动被删除或取消的原因说明 |
| TENANT_ID_ | varchar(255) | 否 | 租户ID，用于多租户环境隔离 |

---

## （2）ACT_HI_PROCINST - 历史流程实例表

**表作用**：记录流程实例的历史信息，包括开始时间、结束时间、耗时、删除原因、业务状态等。流程运行期间和结束后都会保留此表中的记录，用于流程追溯和审计。

### 表结构

```sql
CREATE TABLE `ACT_HI_PROCINST` (
  `ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `BUSINESS_KEY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint DEFAULT NULL,
  `START_USER_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `START_ACT_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `END_ACT_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SUPER_PROCESS_INSTANCE_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT '',
  `NAME_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `CALLBACK_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `CALLBACK_TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `REFERENCE_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `REFERENCE_TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `BUSINESS_STATUS_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `END_USER_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `STATE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `PROC_INST_ID_` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PRO_INST_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_PRO_I_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDX_HI_PRO_SUPER_PROCINST` (`SUPER_PROCESS_INSTANCE_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
```

### 字段说明

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| ID_ | varchar(64) | 是 | 主键ID，历史流程实例的唯一标识 |
| REV_ | int | 否 | 版本号，用于乐观锁控制 |
| PROC_INST_ID_ | varchar(64) | 是 | 流程实例ID，与运行时流程实例ID相同 |
| BUSINESS_KEY_ | varchar(255) | 否 | 业务主键，关联业务系统的唯一标识，如订单号、申请单号等 |
| PROC_DEF_ID_ | varchar(64) | 是 | 流程定义ID，关联ACT_RE_PROCDEF表 |
| START_TIME_ | datetime(3) | 是 | 开始时间，流程实例启动的精确时间 |
| END_TIME_ | datetime(3) | 否 | 结束时间，流程实例完成的精确时间，运行中为空 |
| DURATION_ | bigint | 否 | 持续时间，流程执行的毫秒数（END_TIME_ - START_TIME_） |
| START_USER_ID_ | varchar(255) | 否 | 发起人ID，启动流程的用户ID |
| START_ACT_ID_ | varchar(255) | 否 | 开始节点ID，流程启动时的开始事件ID |
| END_ACT_ID_ | varchar(255) | 否 | 结束节点ID，流程完成时的结束事件ID |
| SUPER_PROCESS_INSTANCE_ID_ | varchar(64) | 否 | 父流程实例ID，如果是子流程，指向父流程实例 |
| DELETE_REASON_ | varchar(4000) | 否 | 删除原因，流程被删除或取消的原因 |
| TENANT_ID_ | varchar(255) | 否 | 租户ID，用于多租户环境隔离 |
| NAME_ | varchar(255) | 否 | 流程实例名称 |
| CALLBACK_ID_ | varchar(255) | 否 | 回调ID，用于流程回调 |
| CALLBACK_TYPE_ | varchar(255) | 否 | 回调类型 |
| REFERENCE_ID_ | varchar(255) | 否 | 引用ID，关联外部业务对象 |
| REFERENCE_TYPE_ | varchar(255) | 否 | 引用类型 |
| PROPAGATED_STAGE_INST_ID_ | varchar(255) | 否 | 传播的阶段实例ID，用于CMMN |
| BUSINESS_STATUS_ | varchar(255) | 否 | 业务状态，自定义的业务状态标识 |
| END_USER_ID_ | varchar(255) | 否 | 结束用户ID，完成流程的用户 |
| STATE_ | varchar(255) | 否 | 流程状态，如：ACTIVE、COMPLETED、TERMINATED等 |

---

## （3）ACT_HI_COMMENT - 历史评论表

**表作用**：记录流程审批过程中的评论、意见、备注等说明性信息。常用于记录审批意见、退回原因等。

### 表结构

```sql
CREATE TABLE `ACT_HI_COMMENT` (
  `ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `TIME_` datetime(3) NOT NULL,
  `USER_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `ACTION_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `MESSAGE_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `FULL_MSG_` longblob,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
```

### 字段说明

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| ID_ | varchar(64) | 是 | 主键ID，评论记录的唯一标识 |
| TYPE_ | varchar(255) | 否 | 评论类型，如：comment、event等 |
| TIME_ | datetime(3) | 是 | 评论时间，添加评论的精确时间 |
| USER_ID_ | varchar(255) | 否 | 评论用户ID，添加评论的用户 |
| TASK_ID_ | varchar(64) | 否 | 任务ID，评论关联的任务 |
| PROC_INST_ID_ | varchar(64) | 否 | 流程实例ID，评论关联的流程实例 |
| ACTION_ | varchar(255) | 否 | 动作类型，如：AddComment、AddAttachment等 |
| MESSAGE_ | varchar(4000) | 否 | 评论消息，简短的评论内容 |
| FULL_MSG_ | longblob | 否 | 完整消息，完整的评论内容（支持大文本） |

---

## （4）ACT_HI_VARINST - 历史变量表

**表作用**：记录流程实例、任务执行过程中变量的历史值。包括流程变量、任务变量等的历史记录，支持多种数据类型。

### 表结构

```sql
CREATE TABLE `ACT_HI_VARINST` (
  `ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8mb3_bin NOT NULL,
  `VAR_TYPE_` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint DEFAULT NULL,
  `TEXT_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `META_INFO_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `LAST_UPDATED_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_PROCVAR_NAME_TYPE` (`NAME_`,`VAR_TYPE_`),
  KEY `ACT_IDX_HI_VAR_SCOPE_ID_TYPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_VAR_SUB_ID_TYPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_PROCVAR_PROC_INST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PROCVAR_TASK_ID` (`TASK_ID_`),
  KEY `ACT_IDX_HI_PROCVAR_EXE` (`EXECUTION_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
```

### 字段说明

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| ID_ | varchar(64) | 是 | 主键ID，变量实例的唯一标识 |
| REV_ | int | 否 | 版本号，用于乐观锁控制 |
| PROC_INST_ID_ | varchar(64) | 否 | 流程实例ID，流程变量关联的流程实例 |
| EXECUTION_ID_ | varchar(64) | 否 | 执行实例ID，局部变量关联的执行实例 |
| TASK_ID_ | varchar(64) | 否 | 任务ID，任务变量关联的任务 |
| NAME_ | varchar(255) | 是 | 变量名，变量的唯一标识符 |
| VAR_TYPE_ | varchar(100) | 否 | 变量类型，如：string、integer、boolean、json等 |
| SCOPE_ID_ | varchar(255) | 否 | 作用域ID，变量的作用域标识 |
| SUB_SCOPE_ID_ | varchar(255) | 否 | 子作用域ID |
| SCOPE_TYPE_ | varchar(255) | 否 | 作用域类型 |
| BYTEARRAY_ID_ | varchar(64) | 否 | 二进制数组ID，存储序列化对象时关联ACT_GE_BYTEARRAY |
| DOUBLE_ | double | 否 | 双精度浮点值，存储浮点数类型变量 |
| LONG_ | bigint | 否 | 长整数值，存储整数类型变量 |
| TEXT_ | varchar(4000) | 否 | 文本值，存储字符串类型变量 |
| TEXT2_ | varchar(4000) | 否 | 文本值2，存储额外文本信息（如JSON） |
| META_INFO_ | varchar(4000) | 否 | 元数据信息，变量的描述和配置信息 |
| CREATE_TIME_ | datetime(3) | 否 | 创建时间，变量创建的时间 |
| LAST_UPDATED_TIME_ | datetime(3) | 否 | 最后更新时间，变量最后修改的时间 |

---

## （5）ACT_HI_DETAIL - 历史详细信息表

**表作用**：记录流程实例运行的细节信息，包括变量更新、表单属性更新等操作的详细记录。

### 表结构

```sql
CREATE TABLE `ACT_HI_DETAIL` (
  `ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `TYPE_` varchar(255) COLLATE utf8mb3_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8mb3_bin NOT NULL,
  `VAR_TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `REV_` int DEFAULT NULL,
  `TIME_` datetime(3) NOT NULL,
  `BYTEARRAY_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint DEFAULT NULL,
  `TEXT_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_DETAIL_PROC_INST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_ACT_INST` (`ACT_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_TIME` (`TIME_`),
  KEY `ACT_IDX_HI_DETAIL_NAME` (`NAME_`),
  KEY `ACT_IDX_HI_DETAIL_TASK_ID` (`TASK_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
```

### 字段说明

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| ID_ | varchar(64) | 是 | 主键ID，详细信息记录的唯一标识 |
| TYPE_ | varchar(255) | 是 | 类型，如：VariableUpdate、FormProperty等 |
| PROC_INST_ID_ | varchar(64) | 否 | 流程实例ID |
| EXECUTION_ID_ | varchar(64) | 否 | 执行实例ID |
| TASK_ID_ | varchar(64) | 否 | 任务ID |
| ACT_INST_ID_ | varchar(64) | 否 | 活动实例ID，关联ACT_HI_ACTINST |
| NAME_ | varchar(255) | 是 | 名称，变量名或表单属性名 |
| VAR_TYPE_ | varchar(255) | 否 | 变量类型 |
| REV_ | int | 否 | 版本号 |
| TIME_ | datetime(3) | 是 | 时间，操作发生的时间 |
| BYTEARRAY_ID_ | varchar(64) | 否 | 二进制数组ID |
| DOUBLE_ | double | 否 | 双精度浮点值 |
| LONG_ | bigint | 否 | 长整数值 |
| TEXT_ | varchar(4000) | 否 | 文本值 |
| TEXT2_ | varchar(4000) | 否 | 文本值2 |

---

## （6）ACT_HI_TASKINST - 历史任务实例表

**表作用**：记录用户任务的历史信息，包括任务名称、办理人、开始时间、结束时间、耗时、优先级、到期时间等。任务完成后记录仍保留在此表中。

### 表结构

```sql
CREATE TABLE `ACT_HI_TASKINST` (
  `ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_DEF_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_DEF_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_DEF_KEY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `STATE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `PARENT_TASK_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `OWNER_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `ASSIGNEE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `IN_PROGRESS_TIME_` datetime(3) DEFAULT NULL,
  `IN_PROGRESS_STARTED_BY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `CLAIM_TIME_` datetime(3) DEFAULT NULL,
  `CLAIMED_BY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SUSPENDED_TIME_` datetime(3) DEFAULT NULL,
  `SUSPENDED_BY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `COMPLETED_BY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `DURATION_` bigint DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) COLLATE utf8mb3_bin DEFAULT NULL,
  `PRIORITY_` int DEFAULT NULL,
  `IN_PROGRESS_DUE_DATE_` datetime(3) DEFAULT NULL,
  `DUE_DATE_` datetime(3) DEFAULT NULL,
  `FORM_KEY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `CATEGORY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT '',
  `LAST_UPDATED_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_TASK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_INST_PROCINST` (`PROC_INST_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
```

### 字段说明

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| ID_ | varchar(64) | 是 | 主键ID，任务实例的唯一标识 |
| REV_ | int | 否 | 版本号，用于乐观锁控制 |
| PROC_DEF_ID_ | varchar(64) | 否 | 流程定义ID |
| TASK_DEF_ID_ | varchar(64) | 否 | 任务定义ID |
| TASK_DEF_KEY_ | varchar(255) | 否 | 任务定义KEY，BPMN中定义的任务ID |
| PROC_INST_ID_ | varchar(64) | 否 | 流程实例ID |
| EXECUTION_ID_ | varchar(64) | 否 | 执行实例ID |
| SCOPE_ID_ | varchar(255) | 否 | 作用域ID |
| SUB_SCOPE_ID_ | varchar(255) | 否 | 子作用域ID |
| SCOPE_TYPE_ | varchar(255) | 否 | 作用域类型 |
| SCOPE_DEFINITION_ID_ | varchar(255) | 否 | 作用域定义ID |
| PROPAGATED_STAGE_INST_ID_ | varchar(255) | 否 | 传播的阶段实例ID |
| STATE_ | varchar(255) | 否 | 任务状态，如：CREATED、ASSIGNED、COMPLETED等 |
| NAME_ | varchar(255) | 否 | 任务名称，用户可见的任务显示名称 |
| PARENT_TASK_ID_ | varchar(64) | 否 | 父任务ID，子任务指向父任务 |
| DESCRIPTION_ | varchar(4000) | 否 | 任务描述，任务的详细说明 |
| OWNER_ | varchar(255) | 否 | 任务所有者，通常是委托任务的原办理人 |
| ASSIGNEE_ | varchar(255) | 否 | 办理人，当前任务的办理用户ID |
| START_TIME_ | datetime(3) | 是 | 开始时间，任务创建的时间 |
| IN_PROGRESS_TIME_ | datetime(3) | 否 | 进行中时间，任务开始处理的时间 |
| IN_PROGRESS_STARTED_BY_ | varchar(255) | 否 | 进行中启动者ID |
| CLAIM_TIME_ | datetime(3) | 否 | 认领时间，任务被认领的时间 |
| CLAIMED_BY_ | varchar(255) | 否 | 认领人ID |
| SUSPENDED_TIME_ | datetime(3) | 否 | 挂起时间 |
| SUSPENDED_BY_ | varchar(255) | 否 | 挂起人ID |
| END_TIME_ | datetime(3) | 否 | 结束时间，任务完成的时间 |
| COMPLETED_BY_ | varchar(255) | 否 | 完成人ID，实际完成该任务的用户 |
| DURATION_ | bigint | 否 | 持续时间，任务执行的毫秒数 |
| DELETE_REASON_ | varchar(4000) | 否 | 删除原因 |
| PRIORITY_ | int | 否 | 优先级，任务的优先级（数字越大优先级越高） |
| IN_PROGRESS_DUE_DATE_ | datetime(3) | 否 | 进行中到期时间 |
| DUE_DATE_ | datetime(3) | 否 | 到期时间，任务的期望完成时间 |
| FORM_KEY_ | varchar(255) | 否 | 表单KEY，关联的表单标识 |
| CATEGORY_ | varchar(255) | 否 | 任务分类 |
| TENANT_ID_ | varchar(255) | 否 | 租户ID |
| LAST_UPDATED_TIME_ | datetime(3) | 否 | 最后更新时间 |

---

## （7）ACT_HI_IDENTITYLINK - 历史身份关联表

**表作用**：记录任务、流程实例与参与者（用户、组）的历史关联关系。包括任务的候选人、候选组、办理人等历史信息。

### 表结构

```sql
CREATE TABLE `ACT_HI_IDENTITYLINK` (
  `ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `GROUP_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `USER_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_USER` (`USER_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_TASK` (`TASK_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_PROCINST` (`PROC_INST_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
```

### 字段说明

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| ID_ | varchar(64) | 是 | 主键ID，身份关联记录的唯一标识 |
| GROUP_ID_ | varchar(255) | 否 | 组ID，关联的用户组 |
| TYPE_ | varchar(255) | 否 | 关联类型，如：candidate（候选人）、participant（参与者）、assignee（办理人）、owner（所有者）等 |
| USER_ID_ | varchar(255) | 否 | 用户ID，关联的用户 |
| TASK_ID_ | varchar(64) | 否 | 任务ID，关联的任务 |
| CREATE_TIME_ | datetime(3) | 否 | 创建时间 |
| PROC_INST_ID_ | varchar(64) | 否 | 流程实例ID |
| SCOPE_ID_ | varchar(255) | 否 | 作用域ID |
| SUB_SCOPE_ID_ | varchar(255) | 否 | 子作用域ID |
| SCOPE_TYPE_ | varchar(255) | 否 | 作用域类型 |
| SCOPE_DEFINITION_ID_ | varchar(255) | 否 | 作用域定义ID |

---

# 2. 运行实例表

运行实例表用于记录流程运行时的状态信息，包括流程实例、任务、变量、作业等。这些表中的数据在流程运行过程中动态变化，流程完成后相关记录会被删除。

## （1）ACT_RU_EXECUTION - 运行时执行实例表

**表作用**：记录流程实例和执行路径的运行时状态。流程启动时会创建根执行实例，流程执行过程中会记录当前活动的节点信息。流程完成后记录会被删除。

### 表结构

```sql
CREATE TABLE `ACT_RU_EXECUTION` (
  `ID_` varchar(64) COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `BUSINESS_KEY_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `PARENT_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `SUPER_EXEC_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `IS_ACTIVE_` tinyint DEFAULT NULL,
  `IS_CONCURRENT_` tinyint DEFAULT NULL,
  `IS_SCOPE_` tinyint DEFAULT NULL,
  `IS_EVENT_SCOPE_` tinyint DEFAULT NULL,
  `IS_MI_ROOT_` tinyint DEFAULT NULL,
  `SUSPENSION_STATE_` int DEFAULT NULL,
  `CACHED_ENT_STATE_` int DEFAULT NULL,
  `TENANT_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT '',
  `NAME_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `START_ACT_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `START_TIME_` datetime(3) DEFAULT NULL,
  `START_USER_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `LOCK_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `IS_COUNT_ENABLED_` tinyint DEFAULT NULL,
  `EVT_SUBSCR_COUNT_` int DEFAULT NULL,
  `TASK_COUNT_` int DEFAULT NULL,
  `JOB_COUNT_` int DEFAULT NULL,
  `TIMER_JOB_COUNT_` int DEFAULT NULL,
  `SUSP_JOB_COUNT_` int DEFAULT NULL,
  `DEADLETTER_JOB_COUNT_` int DEFAULT NULL,
  `EXTERNAL_WORKER_JOB_COUNT_` int DEFAULT NULL,
  `VAR_COUNT_` int DEFAULT NULL,
  `ID_LINK_COUNT_` int DEFAULT NULL,
  `CALLBACK_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `CALLBACK_TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `REFERENCE_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `REFERENCE_TYPE_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  `BUSINESS_STATUS_` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EXEC_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDC_EXEC_ROOT` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_EXEC_REF_ID_` (`REFERENCE_ID_`),
  KEY `ACT_FK_EXE_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_FK_EXE_PARENT` (`PARENT_ID_`),
  KEY `ACT_FK_EXE_SUPER` (`SUPER_EXEC_`),
  KEY `ACT_FK_EXE_PROCDEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_EXE_PARENT` FOREIGN KEY (`PARENT_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE CASCADE,
  CONSTRAINT `ACT_FK_EXE_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`),
  CONSTRAINT `ACT_FK_EXE_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ACT_FK_EXE_SUPER` FOREIGN KEY (`SUPER_EXEC_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
```

### 字段说明

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| ID_ | varchar(64) | 是 | 主键ID，执行实例的唯一标识 |
| REV_ | int | 否 | 版本号，用于乐观锁控制 |
| PROC_INST_ID_ | varchar(64) | 否 | 流程实例ID，根执行实例的ID与PROC_INST_ID_相同 |
| BUSINESS_KEY_ | varchar(255) | 否 | 业务主键，关联业务系统的唯一标识 |
| PARENT_ID_ | varchar(64) | 否 | 父执行实例ID，子执行实例指向父执行实例 |
| PROC_DEF_ID_ | varchar(64) | 否 | 流程定义ID |
| SUPER_EXEC_ | varchar(64) | 否 | 父流程执行实例ID，用于子流程 |
| ROOT_PROC_INST_ID_ | varchar(64) | 否 | 根流程实例ID |
| ACT_ID_ | varchar(255) | 否 | 当前活动节点ID，当前正在执行的BPMN节点 |
| IS_ACTIVE_ | tinyint | 否 | 是否活跃，0-否，1-是 |
| IS_CONCURRENT_ | tinyint | 否 | 是否并发，是否在并发分支中 |
| IS_SCOPE_ | tinyint | 否 | 是否作用域 |
| IS_EVENT_SCOPE_ | tinyint | 否 | 是否事件作用域 |
| IS_MI_ROOT_ | tinyint | 否 | 是否多实例根 |
| SUSPENSION_STATE_ | int | 否 | 挂起状态，1-激活，2-挂起 |
| CACHED_ENT_STATE_ | int | 否 | 缓存实体状态 |
| TENANT_ID_ | varchar(255) | 否 | 租户ID |
| NAME_ | varchar(255) | 否 | 执行实例名称 |
| START_ACT_ID_ | varchar(255) | 否 | 开始节点ID |
| START_TIME_ | datetime(3) | 否 | 开始时间 |
| START_USER_ID_ | varchar(255) | 否 | 发起人ID |
| LOCK_TIME_ | timestamp(3) | 否 | 锁定时间 |
| LOCK_OWNER_ | varchar(255) | 否 | 锁定所有者 |
| IS_COUNT_ENABLED_ | tinyint | 否 | 是否启用计数 |
| EVT_SUBSCR_COUNT_ | int | 否 | 事件订阅数量 |
| TASK_COUNT_ | int | 否 | 任务数量 |
| JOB_COUNT_ | int | 否 | 作业数量 |
| TIMER_JOB_COUNT_ | int | 否 | 定时器作业数量 |
| SUSP_JOB_COUNT_ | int | 否 | 挂起作业数量 |
| DEADLETTER_JOB_COUNT_ | int | 否 | 死信作业数量 |
| EXTERNAL_WORKER_JOB_COUNT_ | int | 否 | 外部工作者作业数量 |
| VAR_COUNT_ | int | 否 | 变量数量 |
| ID_LINK_COUNT_ | int | 否 | 身份关联数量 |
| CALLBACK_ID_ | varchar(255) | 否 | 回调ID |
| CALLBACK_TYPE_ | varchar(255) | 否 | 回调类型 |
| REFERENCE_ID_ | varchar(255) | 否 | 引用ID |
| REFERENCE_TYPE_ | varchar(255) | 否 | 引用类型 |
| PROPAGATED_STAGE_INST_ID_ | varchar(255) | 否 | 传播的阶段实例ID |
| BUSINESS_STATUS_ | varchar(255) | 否 | 业务状态 |

由于篇幅限制，我将在下一部分继续添加其他表的字段说明。是否需要我继续补充剩余表（ACT_RU_TASK、ACT_RU_VARIABLE等）的字段说明?