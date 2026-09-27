USE `test-db`;

DROP TABLE IF EXISTS employee_role;
DROP TABLE IF EXISTS employee_profile;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS dept;

CREATE TABLE dept (
    id          BIGINT       NOT NULL COMMENT '主键',
    name        VARCHAR(64)  NOT NULL COMMENT '部门名称',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '部门';

CREATE TABLE employee (
    id          BIGINT       NOT NULL COMMENT '主键',
    dept_id     BIGINT       NOT NULL COMMENT '部门主键',
    name        VARCHAR(64)  NOT NULL COMMENT '姓名',
    job_title   VARCHAR(64)  NOT NULL COMMENT '岗位',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_employee_dept_id (dept_id),
    CONSTRAINT fk_employee_dept FOREIGN KEY (dept_id) REFERENCES dept (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工';

CREATE TABLE employee_profile (
    id           BIGINT       NOT NULL COMMENT '主键',
    employee_id  BIGINT       NOT NULL COMMENT '员工主键',
    phone        VARCHAR(20)  NOT NULL COMMENT '手机号',
    email        VARCHAR(128) NOT NULL COMMENT '邮箱',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_profile_employee_id (employee_id),
    CONSTRAINT fk_employee_profile_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工档案，与员工一对一';

CREATE TABLE role (
    id          BIGINT      NOT NULL COMMENT '主键',
    code        VARCHAR(32) NOT NULL COMMENT '角色编码',
    name        VARCHAR(64) NOT NULL COMMENT '角色名称',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色';

CREATE TABLE employee_role (
    id          BIGINT   NOT NULL COMMENT '主键',
    employee_id BIGINT   NOT NULL COMMENT '员工主键',
    role_id     BIGINT   NOT NULL COMMENT '角色主键',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_role (employee_id, role_id),
    KEY idx_employee_role_role_id (role_id),
    CONSTRAINT fk_employee_role_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_employee_role_role FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工与角色的多对多中间表';

INSERT INTO dept (id, name) VALUES
    (1, '研发部'),
    (2, '市场部');

INSERT INTO role (id, code, name) VALUES
    (1, 'ADMIN', '管理员'),
    (2, 'DEV', '开发'),
    (3, 'SALES', '销售');

INSERT INTO employee (id, dept_id, name, job_title) VALUES
    (1, 1, '张三', '后端工程师'),
    (2, 1, '李四', '前端工程师'),
    (3, 2, '王五', '销售经理');

INSERT INTO employee_profile (id, employee_id, phone, email) VALUES
    (1, 1, '13800000001', 'zhangsan@example.com'),
    (2, 2, '13800000002', 'lisi@example.com'),
    (3, 3, '13800000003', 'wangwu@example.com');

INSERT INTO employee_role (id, employee_id, role_id) VALUES
    (1, 1, 1),
    (2, 1, 2),
    (3, 2, 2),
    (4, 3, 3);
