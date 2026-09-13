create table chat_memory
(
    id              varchar(64)  not null comment '主键' primary key,
    message_type    varchar(16)  null comment '消息类型',
    message         json         null comment '对话内容',
    conversation_id varchar(64)  null comment '会话ID',
    user_id         varchar(64)  null comment '用户ID',
    username        varchar(128) null comment '用户名',
    aggregation_id  varchar(64)  null comment '聚合ID',
    create_time     timestamp    null,
    update_time     timestamp    null
)
comment '对话记忆';



create table msg_aggregation (
     id           varchar(64) not null comment '主键',
     last_size    float       not null default 0 comment '上次压缩的大小',
     current_size float       not null default 0 comment '本次压缩的大小',
     content      json        not null comment '对话内容',
     compression  int         not null default 0 comment '压缩次数',
     create_time  timestamp   null,
     update_time  timestamp   null
)
comment '对话记忆聚合压缩';
