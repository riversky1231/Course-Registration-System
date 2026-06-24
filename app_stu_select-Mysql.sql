-- 项目唯一数据库脚本。
-- 团队协作时只维护这一份;不要再新增 schema.sql / data.sql。
-- 推荐用法：
-- 1. 在 MySQL 客户端中直接执行整份脚本；
-- 2. 脚本会创建项目默认连接账号 app_stu_select/app_stu_select；
-- 3. 如果你没有建库或建用户权限，先手工创建数据库和账号，再从建表语句开始执行。

CREATE DATABASE IF NOT EXISTS app_stu_select CHARACTER SET utf8mb4;

CREATE USER IF NOT EXISTS 'app_stu_select'@'localhost' IDENTIFIED BY 'app_stu_select';
CREATE USER IF NOT EXISTS 'app_stu_select'@'127.0.0.1' IDENTIFIED BY 'app_stu_select';
GRANT ALL PRIVILEGES ON app_stu_select.* TO 'app_stu_select'@'localhost';
GRANT ALL PRIVILEGES ON app_stu_select.* TO 'app_stu_select'@'127.0.0.1';
FLUSH PRIVILEGES;

USE app_stu_select;

CREATE TABLE IF NOT EXISTS tb_admin (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    username VARCHAR(20) COMMENT '用户名',
    password VARCHAR(100) COMMENT '密码哈希',
    name VARCHAR(18) COMMENT '姓名',
    tele VARCHAR(11) COMMENT '电话',
    UNIQUE KEY uk_tb_admin_username (username)
) COMMENT '管理员表';

CREATE TABLE IF NOT EXISTS tb_teacher (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    username VARCHAR(20) COMMENT '用户名',
    password VARCHAR(100) COMMENT '密码哈希',
    numb VARCHAR(32) COMMENT '教工号',
    tname VARCHAR(18) COMMENT '姓名',
    tbirthday DATETIME COMMENT '出生日期',
    tposition VARCHAR(255) COMMENT '职位',
    ttel VARCHAR(11) COMMENT '电话',
    age INT COMMENT '年龄',
    gender VARCHAR(10) COMMENT '性别',
    UNIQUE KEY uk_tb_teacher_username (username)
) COMMENT '教师表';

CREATE TABLE IF NOT EXISTS tb_student (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    username VARCHAR(20) COMMENT '用户名',
    password VARCHAR(100) COMMENT '密码哈希',
    numb VARCHAR(32) COMMENT '学号',
    sname VARCHAR(255) COMMENT '姓名',
    sdept VARCHAR(255) COMMENT '学院',
    sbirthday DATETIME COMMENT '出生日期',
    tele VARCHAR(11) COMMENT '电话',
    email VARCHAR(255) COMMENT '邮箱',
    ssex VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    smajor VARCHAR(255) COMMENT '专业',
    sclass VARCHAR(255) COMMENT '班级',
    grade INT COMMENT '年级：1=大一,2=大二,3=大三,4=大四',
    enrollment_year INT COMMENT '入学年份',
    UNIQUE KEY uk_tb_student_username (username)
) COMMENT '学生表';

CREATE TABLE IF NOT EXISTS tb_course (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    name VARCHAR(18) COMMENT '课程名称',
    score DOUBLE COMMENT '学分',
    numb VARCHAR(32) COMMENT '编号',
    tid VARCHAR(32) COMMENT '任课教师',
    jianjie VARCHAR(255) COMMENT '课程简介',
    dept VARCHAR(255) COMMENT '开课学院',
    max_students INT DEFAULT 0 COMMENT '选课容量上限，0表示不限制',
    time_slot VARCHAR(64) COMMENT '上课时间段，如"周一第1-2节"',
    course_type VARCHAR(64) COMMENT '课程类型：专业必修/专业选修/通识必修/通识选修',
    grade_limit INT COMMENT '年级限制：1=大一,2=大二,3=大三,4=大四,NULL=不限制',
    UNIQUE KEY uk_tb_course_numb (numb)
) COMMENT '课程表';

CREATE TABLE IF NOT EXISTS tb_sct (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    courseid VARCHAR(32) COMMENT '课程',
    studentId VARCHAR(32) COMMENT '学生',
    teaid VARCHAR(32) COMMENT '批改教师',
    score DOUBLE COMMENT '成绩',
    graded TINYINT(1) DEFAULT 0 COMMENT '是否已录入成绩',
    createtime DATETIME COMMENT '学生选课时间',
    UNIQUE KEY uk_tb_sct_course_student (courseid, studentId),
    KEY idx_tb_sct_courseid (courseid),
    KEY idx_tb_sct_studentid (studentId),
    KEY idx_tb_sct_teaid (teaid)
) COMMENT '学生选课表';

CREATE TABLE IF NOT EXISTS tb_admin_audit_log (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    admin_id VARCHAR(32) COMMENT '操作管理员ID',
    admin_username VARCHAR(20) COMMENT '操作管理员账号',
    action VARCHAR(16) COMMENT '操作类型',
    target_type VARCHAR(32) COMMENT '操作对象类型',
    target_id VARCHAR(32) COMMENT '操作对象ID',
    target_name VARCHAR(255) COMMENT '操作对象名称',
    detail VARCHAR(500) COMMENT '操作详情',
    createtime DATETIME COMMENT '操作时间'
) COMMENT '管理员审计日志表';

CREATE TABLE IF NOT EXISTS tb_notification (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    recipient_id VARCHAR(32) COMMENT '接收人ID',
    recipient_role VARCHAR(20) COMMENT '接收人角色',
    recipient_name VARCHAR(255) COMMENT '接收人名称',
    channel VARCHAR(16) COMMENT '通知渠道，当前统一为SYSTEM站内消息',
    status VARCHAR(16) COMMENT '发送状态',
    title VARCHAR(120) COMMENT '通知标题',
    content VARCHAR(500) COMMENT '通知内容',
    contact VARCHAR(255) COMMENT '联系地址',
    result_message VARCHAR(255) COMMENT '结果说明',
    createtime DATETIME COMMENT '创建时间',
    senttime DATETIME COMMENT '发送时间'
) COMMENT '通知记录表';

CREATE TABLE IF NOT EXISTS tb_selection_window (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    action_type VARCHAR(16) COMMENT '操作类型',
    name VARCHAR(64) COMMENT '窗口名称',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    description VARCHAR(255) COMMENT '说明'
) COMMENT '选退课时间窗口表';

-- ============ 新增：选课智能冲突检测相关表 ============

CREATE TABLE IF NOT EXISTS tb_course_prerequisite (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    course_id VARCHAR(32) NOT NULL COMMENT '课程ID',
    prerequisite_course_id VARCHAR(32) NOT NULL COMMENT '先修课程ID',
    min_score DOUBLE DEFAULT 60 COMMENT '先修课程最低分数要求，默认60分及格',
    UNIQUE KEY uk_course_prerequisite (course_id, prerequisite_course_id),
    KEY idx_course_id (course_id),
    KEY idx_prerequisite_course_id (prerequisite_course_id)
) COMMENT '课程先修关系表';

CREATE TABLE IF NOT EXISTS tb_course_mutex (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    course_id_a VARCHAR(32) NOT NULL COMMENT '课程A的ID',
    course_id_b VARCHAR(32) NOT NULL COMMENT '课程B的ID',
    reason VARCHAR(255) COMMENT '互斥原因说明',
    UNIQUE KEY uk_course_mutex (course_id_a, course_id_b),
    KEY idx_course_id_a (course_id_a),
    KEY idx_course_id_b (course_id_b)
) COMMENT '课程互斥关系表';

CREATE TABLE IF NOT EXISTS tb_course_type_limit (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    course_type VARCHAR(64) NOT NULL COMMENT '课程类型',
    max_courses INT NOT NULL COMMENT '最多可选课程数量',
    description VARCHAR(255) COMMENT '规则说明',
    UNIQUE KEY uk_course_type (course_type)
) COMMENT '课程类型限选规则表';

CREATE TABLE IF NOT EXISTS tb_semester_credit_limit (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    min_gpa DOUBLE NOT NULL COMMENT 'GPA下限（包含）',
    max_gpa DOUBLE NOT NULL COMMENT 'GPA上限（不包含）',
    max_credits DOUBLE NOT NULL COMMENT '允许选课的最大学分',
    description VARCHAR(255) COMMENT '规则说明',
    UNIQUE KEY uk_gpa_range (min_gpa, max_gpa)
) COMMENT '学期学分上限配置表';

-- 所有演示账号初始密码均为 123456，但数据库中保存的是 BCrypt 哈希值。
SET @seed_password_hash = '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q';

-- 仅保留超级管理员账号，确保系统可以正常登录
INSERT INTO tb_admin (id, username, password, name, tele) VALUES
('A1001', 'admin', @seed_password_hash, '系统管理员', '13800000001')
ON DUPLICATE KEY UPDATE
username = VALUES(username),
password = VALUES(password),
name = VALUES(name),
tele = VALUES(tele);

-- ============ 演示数据：教师 ============
INSERT INTO tb_teacher (id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender) VALUES
('T2001', 't_zhang', @seed_password_hash, '2024001', '张若琳', '1987-09-12 00:00:00', '教授', '13810010001', 36, '女'),
('T2002', 't_li', @seed_password_hash, '2024002', '李振华', '1985-03-21 00:00:00', '副教授', '13810010002', 39, '男'),
('T2003', 't_sun', @seed_password_hash, '2024003', '孙启明', '1990-01-16 00:00:00', '讲师', '13810010003', 34, '男')
ON DUPLICATE KEY UPDATE
username = VALUES(username),
password = VALUES(password),
numb = VALUES(numb),
tname = VALUES(tname),
tbirthday = VALUES(tbirthday),
tposition = VALUES(tposition),
ttel = VALUES(ttel),
age = VALUES(age),
gender = VALUES(gender);

-- ============ 演示数据：学生（包含年级信息） ============
INSERT INTO tb_student (id, username, password, numb, sname, sdept, sbirthday, tele, email, ssex, age, smajor, sclass, grade, enrollment_year) VALUES
('S3001', 'stu_chen', @seed_password_hash, '2023001', '陈知远', '信息工程学院', '2004-03-11 00:00:00', '13920010001', 'stu_chen@example.com', '男', 20, '软件工程', '软工2301', 2, 2023),
('S3002', 'stu_lin', @seed_password_hash, '2023002', '林晚晴', '信息工程学院', '2004-06-20 00:00:00', '13920010002', 'stu_lin@example.com', '女', 20, '网络工程', '网工2301', 2, 2023),
('S3003', 'stu_qin', @seed_password_hash, '2024001', '秦语桐', '数据科学学院', '2005-12-09 00:00:00', '13920010003', 'stu_qin@example.com', '女', 18, '数据科学', '数科2401', 1, 2024)
ON DUPLICATE KEY UPDATE
username = VALUES(username),
password = VALUES(password),
numb = VALUES(numb),
sname = VALUES(sname),
sdept = VALUES(sdept),
sbirthday = VALUES(sbirthday),
tele = VALUES(tele),
email = VALUES(email),
ssex = VALUES(ssex),
age = VALUES(age),
smajor = VALUES(smajor),
sclass = VALUES(sclass),
grade = VALUES(grade),
enrollment_year = VALUES(enrollment_year);

-- ============ 演示数据：课程（包含类型和年级限制） ============
INSERT INTO tb_course (id, name, score, numb, tid, jianjie, dept, max_students, time_slot, course_type, grade_limit) VALUES
('C4001', 'Java基础', 3.0, 'K23001', 'T2001', 'Java语言基础入门课程', '信息工程学院', 60, '周一第1-2节', '专业必修', 1),
('C4002', '数据结构', 4.0, 'K23002', 'T2002', '数据结构与算法基础', '信息工程学院', 60, '周二第3-4节', '专业必修', 1),
('C4003', '数据库原理', 3.0, 'K23003', 'T2002', '关系数据库理论与SQL', '信息工程学院', 60, '周三第1-2节', '专业必修', 2),
('C4004', 'Java Web开发', 3.5, 'K23004', 'T2001', 'Web应用开发实战', '信息工程学院', 50, '周四第3-4节', '专业必修', 2),
('C4005', '软件架构设计', 2.5, 'K23005', 'T2003', '软件系统架构与设计模式', '信息工程学院', 40, '周五第1-2节', '专业选修', 3),
('C4006', '分布式系统', 3.0, 'K23006', 'T2001', '分布式系统原理与实践', '信息工程学院', 40, '周一第5-6节', '专业选修', 3),
('C4007', '微服务架构', 3.0, 'K23007', 'T2003', '微服务设计与实现', '信息工程学院', 35, '周二第5-6节', '专业选修', 4),
('C4008', 'Python Web开发', 3.5, 'K23008', 'T2003', 'Python Web框架应用', '信息工程学院', 50, '周四第3-4节', '专业选修', 2),
('C4009', '大学英语', 2.0, 'K23009', 'T2001', '通识英语课程', '外国语学院', 80, '周三第3-4节', '通识必修', NULL),
('C4010', '艺术鉴赏', 1.5, 'K23010', 'T2002', '艺术与美学入门', '艺术学院', 100, '周五第5-6节', '通识选修', NULL),
('C4011', '创新创业', 1.5, 'K23011', 'T2003', '创业思维与实践', '创新学院', 60, '周三第7-8节', '通识选修', NULL),
('C4012', '音乐欣赏', 1.5, 'K23012', 'T2001', '音乐基础与鉴赏', '艺术学院', 100, '周四第7-8节', '通识选修', NULL),
('C9001', 'Mantis重复选课测试', 1.0, 'BUG-DUP-001', 'T2001', '实验三第一轮缺陷复现课程：用于验证重复选课判断条件被反转后的拦截问题', '质量审计实验', 30, '周六第3-4节', '缺陷复现', NULL),
('C9002', 'Mantis容量边界测试', 1.0, 'BUG-CAP-001', 'T2001', '实验三第二轮缺陷复现课程：容量为1且预置1条选课记录，用于验证满员边界判断', '质量审计实验', 1, '周六第5-6节', '缺陷复现', NULL)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
score = VALUES(score),
numb = VALUES(numb),
tid = VALUES(tid),
jianjie = VALUES(jianjie),
dept = VALUES(dept),
max_students = VALUES(max_students),
time_slot = VALUES(time_slot),
course_type = VALUES(course_type),
grade_limit = VALUES(grade_limit);

-- ============ 演示数据：先修课程关系 ============
INSERT INTO tb_course_prerequisite (id, course_id, prerequisite_course_id, min_score) VALUES
('P1001', 'C4003', 'C4002', 60),  -- 数据库原理 需要先修 数据结构
('P1002', 'C4004', 'C4001', 60),  -- Java Web开发 需要先修 Java基础
('P1003', 'C4005', 'C4004', 70),  -- 软件架构设计 需要先修 Java Web开发(70分以上)
('P1004', 'C4006', 'C4003', 60),  -- 分布式系统 需要先修 数据库原理
('P1005', 'C4007', 'C4006', 65)   -- 微服务架构 需要先修 分布式系统(65分以上)
ON DUPLICATE KEY UPDATE
course_id = VALUES(course_id),
prerequisite_course_id = VALUES(prerequisite_course_id),
min_score = VALUES(min_score);

-- ============ 演示数据：互斥课程关系 ============
INSERT INTO tb_course_mutex (id, course_id_a, course_id_b, reason) VALUES
('M2001', 'C4004', 'C4008', 'Java Web开发 与 Python Web开发 为同类型课程，只能选其一'),
('M2002', 'C4008', 'C4004', 'Python Web开发 与 Java Web开发 为同类型课程，只能选其一')
ON DUPLICATE KEY UPDATE
course_id_a = VALUES(course_id_a),
course_id_b = VALUES(course_id_b),
reason = VALUES(reason);

-- ============ 演示数据：课程类型限选规则 ============
INSERT INTO tb_course_type_limit (id, course_type, max_courses, description) VALUES
('L3001', '通识选修', 3, '每学期最多选3门通识选修课'),
('L3002', '专业选修', 5, '每学期最多选5门专业选修课')
ON DUPLICATE KEY UPDATE
course_type = VALUES(course_type),
max_courses = VALUES(max_courses),
description = VALUES(description);

-- ============ 演示数据：学期学分上限配置 ============
INSERT INTO tb_semester_credit_limit (id, min_gpa, max_gpa, max_credits, description) VALUES
('CL4001', 0.0, 2.0, 15.0, 'GPA低于2.0，学业预警，限制选课学分'),
('CL4002', 2.0, 3.0, 20.0, 'GPA 2.0-3.0，正常学分上限'),
('CL4003', 3.0, 3.5, 25.0, 'GPA 3.0-3.5，优秀学生可多选学分'),
('CL4004', 3.5, 5.0, 30.0, 'GPA 3.5以上，学霸特权，最高学分上限')
ON DUPLICATE KEY UPDATE
min_gpa = VALUES(min_gpa),
max_gpa = VALUES(max_gpa),
max_credits = VALUES(max_credits),
description = VALUES(description);

-- ============ 演示数据：选课记录（符合业务逻辑的完整数据） ============

-- 重置 Mantis 缺陷复现实验课程的选课记录，保证重复导入后仍可稳定复现
DELETE FROM tb_sct WHERE courseid IN ('C9001', 'C9002');

-- 【学生1：陈知远 (S3001, 大二)】
-- 上学期（大一下）已修课程：Java基础、数据结构、大学英语（已录入成绩）
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) VALUES
('R5001', 'C4001', 'S3001', 'T2001', 85, 1, '2024-03-01 09:00:00'),  -- Java基础 85分（已录入成绩）
('R5002', 'C4002', 'S3001', 'T2002', 90, 1, '2024-03-01 10:00:00'),  -- 数据结构 90分（已录入成绩）
('R5003', 'C4009', 'S3001', 'T2001', 78, 1, '2024-03-01 11:00:00')   -- 大学英语 78分（已录入成绩）
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- 本学期（大二上）正在上的课程：数据库原理、Java Web开发、艺术鉴赏（未录入成绩）
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) VALUES
('R5004', 'C4003', 'S3001', 'T2002', NULL, 0, '2024-09-01 09:00:00'),  -- 数据库原理（本学期选课，未录入成绩）
('R5005', 'C4004', 'S3001', 'T2001', NULL, 0, '2024-09-01 10:00:00'),  -- Java Web开发（本学期选课，未录入成绩）
('R5006', 'C4010', 'S3001', 'T2002', NULL, 0, '2024-09-01 11:00:00')   -- 艺术鉴赏（本学期选课，未录入成绩）
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- 【学生2：林晚晴 (S3002, 大二)】
-- 上学期（大一下）已修课程：Java基础、数据结构（已录入成绩）
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) VALUES
('R5007', 'C4001', 'S3002', 'T2001', 75, 1, '2024-03-01 09:00:00'),  -- Java基础 75分（已录入成绩）
('R5008', 'C4002', 'S3002', 'T2002', 65, 1, '2024-03-01 10:00:00'),  -- 数据结构 65分（已录入成绩，刚及格）
('R5009', 'C4009', 'S3002', 'T2001', 82, 1, '2024-03-01 11:00:00')   -- 大学英语 82分（已录入成绩）
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- 本学期（大二上）正在上的课程：数据库原理、Python Web开发（未录入成绩）
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) VALUES
('R5010', 'C4003', 'S3002', 'T2002', NULL, 0, '2024-09-01 09:00:00'),  -- 数据库原理（本学期选课，未录入成绩）
('R5011', 'C4008', 'S3002', 'T2003', NULL, 0, '2024-09-01 10:00:00'),  -- Python Web开发（本学期选课，未录入成绩）
('R5012', 'C4011', 'S3002', 'T2003', NULL, 0, '2024-09-01 11:00:00')   -- 创新创业（本学期选课，未录入成绩）
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- 【学生3：秦语桐 (S3003, 大一)】
-- 本学期（大一上）正在上的课程：Java基础、大学英语（未录入成绩）
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) VALUES
('R5013', 'C4001', 'S3003', 'T2001', NULL, 0, '2024-09-01 09:00:00'),  -- Java基础（本学期选课，未录入成绩）
('R5014', 'C4009', 'S3003', 'T2001', NULL, 0, '2024-09-01 10:00:00'),  -- 大学英语（本学期选课，未录入成绩）
('R5015', 'C4012', 'S3003', 'T2001', NULL, 0, '2024-09-01 11:00:00')   -- 音乐欣赏（本学期选课，未录入成绩）
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- 【Mantis实验三：容量边界缺陷复现基线】
-- C9002 容量为1，这里预置1条选课记录，使课程刚好满员。
-- 正确代码 currentCount >= maxStudents 会拦截后续选课；
-- 植入缺陷 currentCount > maxStudents 会错误放行第2名学生。
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) VALUES
('R9002', 'C9002', 'S3001', 'T2001', NULL, 0, '2026-06-01 09:00:00')
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- ============ 演示数据：选课时间窗口（长期开放，便于本地测试） ============
INSERT INTO tb_selection_window (id, action_type, name, start_time, end_time, enabled, description) VALUES
('W6001', 'SELECT', '演示选课窗口', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, '长期开放，便于本地功能测试'),
('W6002', 'DROP', '演示退课窗口', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, '长期开放，便于本地功能测试')
ON DUPLICATE KEY UPDATE
action_type = VALUES(action_type),
name = VALUES(name),
start_time = VALUES(start_time),
end_time = VALUES(end_time),
enabled = VALUES(enabled),
description = VALUES(description);
