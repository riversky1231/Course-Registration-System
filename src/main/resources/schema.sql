CREATE TABLE IF NOT EXISTS tb_admin (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    username VARCHAR(20) COMMENT '用户名',
    password VARCHAR(20) COMMENT '密码',
    name VARCHAR(18) COMMENT '姓名',
    tele VARCHAR(11) COMMENT '电话'
) COMMENT '管理员表';

CREATE TABLE IF NOT EXISTS tb_teacher (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    username VARCHAR(20) COMMENT '用户名',
    password VARCHAR(20) COMMENT '密码',
    numb VARCHAR(32) COMMENT '教工号',
    tname VARCHAR(18) COMMENT '姓名',
    tbirthday DATETIME COMMENT '出生日期',
    tposition VARCHAR(255) COMMENT '职位',
    ttel VARCHAR(11) COMMENT '电话',
    age INT COMMENT '年龄',
    gender VARCHAR(10) COMMENT '性别'
) COMMENT '教师表';

CREATE TABLE IF NOT EXISTS tb_student (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    username VARCHAR(20) COMMENT '用户名',
    password VARCHAR(20) COMMENT '密码',
    numb VARCHAR(32) COMMENT '学号',
    sname VARCHAR(255) COMMENT '姓名',
    sdept VARCHAR(255) COMMENT '学院',
    sbirthday DATETIME COMMENT '出生日期',
    tele VARCHAR(11) COMMENT '电话',
    ssex VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    smajor VARCHAR(255) COMMENT '专业',
    sclass VARCHAR(255) COMMENT '班级'
) COMMENT '学生表';

CREATE TABLE IF NOT EXISTS tb_course (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    name VARCHAR(18) COMMENT '课程名称',
    score DOUBLE COMMENT '学分',
    numb VARCHAR(32) COMMENT '编号',
    tid VARCHAR(32) COMMENT '任课教师',
    jianjie VARCHAR(255) COMMENT '课程简介'
) COMMENT '课程表';

CREATE TABLE IF NOT EXISTS tb_sct (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    courseid VARCHAR(32) COMMENT '课程',
    studentId VARCHAR(32) COMMENT '学生',
    teaid VARCHAR(32) COMMENT '批改教师',
    score DOUBLE COMMENT '成绩',
    createtime DATETIME COMMENT '学生选课时间'
) COMMENT '学生选课表';
