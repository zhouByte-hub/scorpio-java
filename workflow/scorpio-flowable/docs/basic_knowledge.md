# 1. 部署涉及的核心表
（1）ACT_RE_DEPLOYMENT
```sql
-- auto-generated definition
create table ACT_RE_DEPLOYMENT
(
    ID_                   varchar(64)             not null
        primary key,
    NAME_                 varchar(255)            null,
    CATEGORY_             varchar(255)            null,
    KEY_                  varchar(255)            null,
    TENANT_ID_            varchar(255) default '' null,
    DEPLOY_TIME_          timestamp(3)            null,
    DERIVED_FROM_         varchar(64)             null,
    DERIVED_FROM_ROOT_    varchar(64)             null,
    PARENT_DEPLOYMENT_ID_ varchar(255)            null,
    ENGINE_VERSION_       varchar(255)            null
)
    collate = utf8mb3_bin;
```
**表作用**：部署信息表，记录流程部署的基本信息，包括部署名称、分类、部署时间等。每次部署流程定义时，都会在此表中生成一条记录。

（2）ACT_RE_PROCDEF
```sql
-- auto-generated definition
create table ACT_RE_PROCDEF
(
    ID_                     varchar(64)             not null
        primary key,
    REV_                    int                     null,
    CATEGORY_               varchar(255)            null,
    NAME_                   varchar(255)            null,
    KEY_                    varchar(255)            not null,
    VERSION_                int                     not null,
    DEPLOYMENT_ID_          varchar(64)             null,
    RESOURCE_NAME_          varchar(4000)           null,
    DGRM_RESOURCE_NAME_     varchar(4000)           null,
    DESCRIPTION_            varchar(4000)           null,
    HAS_START_FORM_KEY_     tinyint                 null,
    HAS_GRAPHICAL_NOTATION_ tinyint                 null,
    SUSPENSION_STATE_       int                     null,
    TENANT_ID_              varchar(255) default '' null,
    ENGINE_VERSION_         varchar(255)            null,
    DERIVED_FROM_           varchar(64)             null,
    DERIVED_FROM_ROOT_      varchar(64)             null,
    DERIVED_VERSION_        int          default 0  not null,
    constraint ACT_UNIQ_PROCDEF
        unique (KEY_, VERSION_, DERIVED_VERSION_, TENANT_ID_)
)
    collate = utf8mb3_bin;
```
**表作用**：流程定义表，记录流程定义的详细信息，包括流程名称、版本、部署ID、资源名称等。同一个流程KEY的不同版本会在此表中生成多条记录，版本号递增。

（3）ACT_GE_BYTEARRAY
```sql
-- auto-generated definition
create table ACT_GE_BYTEARRAY
(
    ID_            varchar(64)  not null
        primary key,
    REV_           int          null,
    NAME_          varchar(255) null,
    DEPLOYMENT_ID_ varchar(64)  null,
    BYTES_         longblob     null,
    GENERATED_     tinyint      null,
    constraint ACT_FK_BYTEARR_DEPL
        foreign key (DEPLOYMENT_ID_) references ACT_RE_DEPLOYMENT (ID_)
)
    collate = utf8mb3_bin;

create index ACT_IDX_BYTEAR_DEPL
    on ACT_GE_BYTEARRAY (DEPLOYMENT_ID_);
```
**表作用**：二进制资源表，存储流程定义文件（BPMN XML）、流程图、表单定义等二进制资源。通过DEPLOYMENT_ID_关联到部署信息。

# 2. 启动流程涉及的核心表
（1）ACT_RU_TASK
```sql
-- auto-generated definition
create table ACT_RU_TASK
(
    ID_                       varchar(64)             not null
        primary key,
    REV_                      int                     null,
    EXECUTION_ID_             varchar(64)             null,
    PROC_INST_ID_             varchar(64)             null,
    PROC_DEF_ID_              varchar(64)             null,
    TASK_DEF_ID_              varchar(64)             null,
    SCOPE_ID_                 varchar(255)            null,
    SUB_SCOPE_ID_             varchar(255)            null,
    SCOPE_TYPE_               varchar(255)            null,
    SCOPE_DEFINITION_ID_      varchar(255)            null,
    PROPAGATED_STAGE_INST_ID_ varchar(255)            null,
    STATE_                    varchar(255)            null,
    NAME_                     varchar(255)            null,
    PARENT_TASK_ID_           varchar(64)             null,
    DESCRIPTION_              varchar(4000)           null,
    TASK_DEF_KEY_             varchar(255)            null,
    OWNER_                    varchar(255)            null,
    ASSIGNEE_                 varchar(255)            null,
    DELEGATION_               varchar(64)             null,
    PRIORITY_                 int                     null,
    CREATE_TIME_              timestamp(3)            null,
    IN_PROGRESS_TIME_         datetime(3)             null,
    IN_PROGRESS_STARTED_BY_   varchar(255)            null,
    CLAIM_TIME_               datetime(3)             null,
    CLAIMED_BY_               varchar(255)            null,
    SUSPENDED_TIME_           datetime(3)             null,
    SUSPENDED_BY_             varchar(255)            null,
    IN_PROGRESS_DUE_DATE_     datetime(3)             null,
    DUE_DATE_                 datetime(3)             null,
    CATEGORY_                 varchar(255)            null,
    SUSPENSION_STATE_         int                     null,
    TENANT_ID_                varchar(255) default '' null,
    FORM_KEY_                 varchar(255)            null,
    IS_COUNT_ENABLED_         tinyint                 null,
    VAR_COUNT_                int                     null,
    ID_LINK_COUNT_            int                     null,
    SUB_TASK_COUNT_           int                     null,
    constraint ACT_FK_TASK_EXE
        foreign key (EXECUTION_ID_) references ACT_RU_EXECUTION (ID_),
    constraint ACT_FK_TASK_PROCDEF
        foreign key (PROC_DEF_ID_) references ACT_RE_PROCDEF (ID_),
    constraint ACT_FK_TASK_PROCINST
        foreign key (PROC_INST_ID_) references ACT_RU_EXECUTION (ID_)
)
    collate = utf8mb3_bin;

create index ACT_IDX_TASK_CREATE
    on ACT_RU_TASK (CREATE_TIME_);

create index ACT_IDX_TASK_SCOPE
    on ACT_RU_TASK (SCOPE_ID_, SCOPE_TYPE_);

create index ACT_IDX_TASK_SCOPE_DEF
    on ACT_RU_TASK (SCOPE_DEFINITION_ID_, SCOPE_TYPE_);

create index ACT_IDX_TASK_SUB_SCOPE
    on ACT_RU_TASK (SUB_SCOPE_ID_, SCOPE_TYPE_);
```
**表作用**：运行时任务表，记录当前活跃的用户任务（待办任务）。包含任务名称、办理人、创建时间、到期时间等信息。任务完成后记录会被删除。

（2）ACT_RU_EXECUTION
```sql
-- auto-generated definition
create table ACT_RU_EXECUTION
(
    ID_                        varchar(64)             not null
        primary key,
    REV_                       int                     null,
    PROC_INST_ID_              varchar(64)             null,
    BUSINESS_KEY_              varchar(255)            null,
    PARENT_ID_                 varchar(64)             null,
    PROC_DEF_ID_               varchar(64)             null,
    SUPER_EXEC_                varchar(64)             null,
    ROOT_PROC_INST_ID_         varchar(64)             null,
    ACT_ID_                    varchar(255)            null,
    IS_ACTIVE_                 tinyint                 null,
    IS_CONCURRENT_             tinyint                 null,
    IS_SCOPE_                  tinyint                 null,
    IS_EVENT_SCOPE_            tinyint                 null,
    IS_MI_ROOT_                tinyint                 null,
    SUSPENSION_STATE_          int                     null,
    CACHED_ENT_STATE_          int                     null,
    TENANT_ID_                 varchar(255) default '' null,
    NAME_                      varchar(255)            null,
    START_ACT_ID_              varchar(255)            null,
    START_TIME_                datetime(3)             null,
    START_USER_ID_             varchar(255)            null,
    LOCK_TIME_                 timestamp(3)            null,
    LOCK_OWNER_                varchar(255)            null,
    IS_COUNT_ENABLED_          tinyint                 null,
    EVT_SUBSCR_COUNT_          int                     null,
    TASK_COUNT_                int                     null,
    JOB_COUNT_                 int                     null,
    TIMER_JOB_COUNT_           int                     null,
    SUSP_JOB_COUNT_            int                     null,
    DEADLETTER_JOB_COUNT_      int                     null,
    EXTERNAL_WORKER_JOB_COUNT_ int                     null,
    VAR_COUNT_                 int                     null,
    ID_LINK_COUNT_             int                     null,
    CALLBACK_ID_               varchar(255)            null,
    CALLBACK_TYPE_             varchar(255)            null,
    REFERENCE_ID_              varchar(255)            null,
    REFERENCE_TYPE_            varchar(255)            null,
    PROPAGATED_STAGE_INST_ID_  varchar(255)            null,
    BUSINESS_STATUS_           varchar(255)            null,
    constraint ACT_FK_EXE_PARENT
        foreign key (PARENT_ID_) references ACT_RU_EXECUTION (ID_)
            on delete cascade,
    constraint ACT_FK_EXE_PROCDEF
        foreign key (PROC_DEF_ID_) references ACT_RE_PROCDEF (ID_),
    constraint ACT_FK_EXE_PROCINST
        foreign key (PROC_INST_ID_) references ACT_RU_EXECUTION (ID_)
            on update cascade on delete cascade,
    constraint ACT_FK_EXE_SUPER
        foreign key (SUPER_EXEC_) references ACT_RU_EXECUTION (ID_)
            on delete cascade
)
    collate = utf8mb3_bin;

create index ACT_IDC_EXEC_ROOT
    on ACT_RU_EXECUTION (ROOT_PROC_INST_ID_);

create index ACT_IDX_EXEC_BUSKEY
    on ACT_RU_EXECUTION (BUSINESS_KEY_);

create index ACT_IDX_EXEC_REF_ID_
    on ACT_RU_EXECUTION (REFERENCE_ID_);
```
**表作用**：运行时执行实例表，记录流程实例和执行路径的运行时状态。流程启动时会创建根执行实例，流程执行过程中会记录当前活动的节点信息。流程完成后记录会被删除。

（3）ACT_HI_PROCINST
```sql
-- auto-generated definition
create table ACT_HI_PROCINST
(
    ID_                        varchar(64)             not null
        primary key,
    REV_                       int          default 1  null,
    PROC_INST_ID_              varchar(64)             not null,
    BUSINESS_KEY_              varchar(255)            null,
    PROC_DEF_ID_               varchar(64)             not null,
    START_TIME_                datetime(3)             not null,
    END_TIME_                  datetime(3)             null,
    DURATION_                  bigint                  null,
    START_USER_ID_             varchar(255)            null,
    START_ACT_ID_              varchar(255)            null,
    END_ACT_ID_                varchar(255)            null,
    SUPER_PROCESS_INSTANCE_ID_ varchar(64)             null,
    DELETE_REASON_             varchar(4000)           null,
    TENANT_ID_                 varchar(255) default '' null,
    NAME_                      varchar(255)            null,
    CALLBACK_ID_               varchar(255)            null,
    CALLBACK_TYPE_             varchar(255)            null,
    REFERENCE_ID_              varchar(255)            null,
    REFERENCE_TYPE_            varchar(255)            null,
    PROPAGATED_STAGE_INST_ID_  varchar(255)            null,
    BUSINESS_STATUS_           varchar(255)            null,
    END_USER_ID_               varchar(255)            null,
    STATE_                     varchar(255)            null,
    constraint PROC_INST_ID_
        unique (PROC_INST_ID_)
)
    collate = utf8mb3_bin;

create index ACT_IDX_HI_PRO_INST_END
    on ACT_HI_PROCINST (END_TIME_);

create index ACT_IDX_HI_PRO_I_BUSKEY
    on ACT_HI_PROCINST (BUSINESS_KEY_);

create index ACT_IDX_HI_PRO_SUPER_PROCINST
    on ACT_HI_PROCINST (SUPER_PROCESS_INSTANCE_ID_);
```
**表作用**：历史流程实例表，记录流程实例的历史信息，包括开始时间、结束时间、耗时、删除原因等。流程运行期间和结束后都会保留此表中的记录，用于流程追溯和审计。

（4）ACT_RU_VARIABLE
```sql
-- auto-generated definition
create table ACT_RU_VARIABLE
(
    ID_           varchar(64)   not null
        primary key,
    REV_          int           null,
    TYPE_         varchar(255)  not null,
    NAME_         varchar(255)  not null,
    EXECUTION_ID_ varchar(64)   null,
    PROC_INST_ID_ varchar(64)   null,
    TASK_ID_      varchar(64)   null,
    SCOPE_ID_     varchar(255)  null,
    SUB_SCOPE_ID_ varchar(255)  null,
    SCOPE_TYPE_   varchar(255)  null,
    BYTEARRAY_ID_ varchar(64)   null,
    DOUBLE_       double        null,
    LONG_         bigint        null,
    TEXT_         varchar(4000) null,
    TEXT2_        varchar(4000) null,
    META_INFO_    varchar(4000) null,
    constraint ACT_FK_VAR_BYTEARRAY
        foreign key (BYTEARRAY_ID_) references ACT_GE_BYTEARRAY (ID_),
    constraint ACT_FK_VAR_EXE
        foreign key (EXECUTION_ID_) references ACT_RU_EXECUTION (ID_),
    constraint ACT_FK_VAR_PROCINST
        foreign key (PROC_INST_ID_) references ACT_RU_EXECUTION (ID_)
)
    collate = utf8mb3_bin;

create index ACT_IDX_RU_VAR_SCOPE_ID_TYPE
    on ACT_RU_VARIABLE (SCOPE_ID_, SCOPE_TYPE_);

create index ACT_IDX_RU_VAR_SUB_ID_TYPE
    on ACT_RU_VARIABLE (SUB_SCOPE_ID_, SCOPE_TYPE_);

create index ACT_IDX_VARIABLE_TASK_ID
    on ACT_RU_VARIABLE (TASK_ID_);
```
**表作用**：运行时变量表，存储流程实例、执行实例、任务执行过程中的变量数据。包括流程变量、任务变量等，支持多种数据类型（字符串、数字、二进制等）。流程完成后记录会被删除。