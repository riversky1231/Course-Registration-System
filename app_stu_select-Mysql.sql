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

-- 审计日志、消息中心模块已下线；若旧库中存在历史表，重新执行脚本时一并清除。
DROP TABLE IF EXISTS tb_admin_audit_log;
DROP TABLE IF EXISTS tb_notification;

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

-- ============ 新增：课程评价（评教）模块表 ============

CREATE TABLE IF NOT EXISTS tb_course_evaluation (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键',
    course_id VARCHAR(32) NOT NULL COMMENT '课程ID',
    student_id VARCHAR(32) NOT NULL COMMENT '评价学生ID',
    teacher_id VARCHAR(32) COMMENT '被评价教师ID（评价时课程的任课教师）',
    rating INT NOT NULL COMMENT '评分：1~5星',
    comment VARCHAR(500) COMMENT '评价内容',
    anonymous TINYINT(1) DEFAULT 0 COMMENT '是否匿名：1=匿名,0=实名',
    create_time DATETIME COMMENT '评价时间',
    UNIQUE KEY uk_course_evaluation (course_id, student_id),
    KEY idx_ce_course_id (course_id),
    KEY idx_ce_student_id (student_id),
    KEY idx_ce_teacher_id (teacher_id)
) COMMENT '课程评价（评教）表';

-- 所有演示账号初始密码均为 123456，但数据库中保存的是 BCrypt 哈希值。
SET @seed_password_hash = '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q';

-- 管理员账号（密码均为 123456）
INSERT INTO tb_admin (id, username, password, name, tele) VALUES
('A1001', 'admin', @seed_password_hash, '系统管理员', '13800000001'),
('A1002', 'admin_jiaowu', @seed_password_hash, '教务管理员', '13800000002'),
('A1003', 'admin_audit', @seed_password_hash, '运维管理员', '13800000003'),
('A1004', 'admin_xuanke', @seed_password_hash, '选课管理员', '13800000004'),
('A1005', 'admin_super', @seed_password_hash, '超级管理员', '13800000005')
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

-- 批量生成教师 T2004~T2030（与上面 3 位合计 30 位，登录密码均为 123456）
INSERT INTO tb_teacher (id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender)
WITH RECURSIVE seq (n) AS (
  SELECT 4
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
  CONCAT('T', 2000 + n),
  CONCAT('teacher', LPAD(n, 2, '0')),
  @seed_password_hash,
  CONCAT('2024', LPAD(n, 3, '0')),
  CONCAT(
    ELT(1 + MOD(n, 12), '王', '李', '张', '刘', '陈', '杨', '黄', '赵', '周', '吴', '徐', '孙'),
    ELT(1 + FLOOR(n / 12), '思', '承', '景'),
    ELT(1 + MOD(n * 5, 12), '安', '博', '辰', '达', '峰', '涵', '杰', '凯', '霖', '铭', '宁', '睿')),
  DATE_ADD('1978-01-01', INTERVAL n * 97 DAY),
  ELT(1 + MOD(n, 4), '教授', '副教授', '讲师', '助教'),
  CONCAT('1381001', LPAD(n, 4, '0')),
  32 + MOD(n, 25),
  IF(MOD(n, 2) = 0, '男', '女')
FROM seq
ON DUPLICATE KEY UPDATE
username = VALUES(username),
password = VALUES(password),
numb = VALUES(numb),
tname = VALUES(tname),
tposition = VALUES(tposition),
tbirthday = VALUES(tbirthday),
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

-- 批量生成学生 S3004~S3080（与上面 3 位合计 80 位，登录密码均为 123456）
INSERT INTO tb_student (id, username, password, numb, sname, sdept, sbirthday, tele, email, ssex, age, smajor, sclass, grade, enrollment_year)
WITH RECURSIVE seq (n) AS (
  SELECT 4
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 80
)
SELECT
  CONCAT('S', 3000 + n),
  CONCAT('student', LPAD(n, 2, '0')),
  @seed_password_hash,
  CONCAT('2023', LPAD(n, 3, '0')),
  CONCAT(
    ELT(1 + MOD(n, 12), '王', '李', '张', '刘', '陈', '杨', '黄', '赵', '周', '吴', '郑', '冯'),
    ELT(1 + FLOOR(n / 12), '沐', '若', '承', '芷', '思', '景', '怀'),
    ELT(1 + MOD(n * 5, 16), '轩', '怡', '航', '琪', '然', '萱', '熙', '婷', '铭', '涵', '豪', '馨', '宇', '宁', '辰', '悦')),
  ELT(1 + MOD(n, 5), '信息工程学院', '数据科学学院', '外国语学院', '艺术学院', '创新学院'),
  DATE_ADD('2003-01-01', INTERVAL n * 53 DAY),
  CONCAT('1392001', LPAD(n, 4, '0')),
  CONCAT('student', LPAD(n, 2, '0'), '@example.com'),
  IF(MOD(n, 2) = 0, '男', '女'),
  18 + MOD(n, 5),
  ELT(1 + MOD(n, 6), '软件工程', '网络工程', '数据科学', '人工智能', '英语', '视觉传达'),
  CONCAT(ELT(1 + MOD(n, 6), '软工', '网工', '数科', '智能', '英语', '视传'), 2300 + MOD(n, 4) + 1),
  1 + MOD(n, 4),
  2023 + MOD(n, 4)
FROM seq
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
('C4011', '创新创业', 1.5, 'K23011', 'T2003', '创业思维与实践', '创新学院', 80, '周三第7-8节', '通识选修', NULL),
('C4012', '音乐欣赏', 1.5, 'K23012', 'T2001', '音乐基础与鉴赏', '艺术学院', 100, '周四第7-8节', '通识选修', NULL)
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

-- ============ 演示数据：补全批量教师的授课课程 ============
-- T2001~T2003 已在核心课程中承担教学；这里为 T2004~T2030 补充专题课，
-- 避免教师账号存在但没有课程、学生、成绩和评教数据。
INSERT INTO tb_course (id, name, score, numb, tid, jianjie, dept, max_students, time_slot, course_type, grade_limit)
WITH RECURSIVE seq (n) AS (
  SELECT 4
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
  CONCAT('C42', LPAD(n, 2, '0')),
  CONCAT(ELT(1 + MOD(n, 8), '软件工程', '数据分析', '网络安全', '智能系统', '英语表达', '视觉设计', '创新实践', '项目管理'), '专题', LPAD(n, 2, '0')),
  1.5 + MOD(n, 3) * 0.5,
  CONCAT('K24T', LPAD(n, 3, '0')),
  CONCAT('T', 2000 + n),
  CONCAT('面向跨专业学生的专题实践课，补全教师 T', 2000 + n, ' 的授课链路'),
  ELT(1 + MOD(n, 5), '信息工程学院', '数据科学学院', '外国语学院', '艺术学院', '创新学院'),
  45,
  CONCAT('线上专题', LPAD(n, 2, '0')),
  IF(MOD(n, 4) = 0, '通识选修', '专业选修'),
  NULL
FROM seq
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

-- 清理历史测试课程 C9001/C9002（Mantis 缺陷复现实验）及其选课记录
DELETE FROM tb_sct WHERE courseid IN ('C9001', 'C9002');
DELETE FROM tb_course WHERE id IN ('C9001', 'C9002');

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

-- 【高年级学生：补全先修链路与高级课程在修记录】
-- 让 C4005/C4006/C4007 不再只存在于先修配置中，而是拥有真实学生、教师、成绩依赖。
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) VALUES
('RA5101', 'C4001', 'S3006', 'T2001', 82, 1, '2024-03-02 09:00:00'),
('RA5102', 'C4002', 'S3006', 'T2002', 79, 1, '2024-03-02 09:05:00'),
('RA5103', 'C4003', 'S3006', 'T2002', 76, 1, '2024-07-02 09:00:00'),
('RA5104', 'C4004', 'S3006', 'T2001', 74, 1, '2024-07-02 09:05:00'),
('RA5105', 'C4005', 'S3006', 'T2003', NULL, 0, '2025-03-02 09:00:00'),
('RA5111', 'C4001', 'S3007', 'T2001', 88, 1, '2024-03-03 09:00:00'),
('RA5112', 'C4002', 'S3007', 'T2002', 84, 1, '2024-03-03 09:05:00'),
('RA5113', 'C4003', 'S3007', 'T2002', 81, 1, '2024-07-03 09:00:00'),
('RA5114', 'C4004', 'S3007', 'T2001', 86, 1, '2024-07-03 09:05:00'),
('RA5115', 'C4005', 'S3007', 'T2003', 83, 1, '2025-01-10 09:00:00'),
('RA5116', 'C4006', 'S3007', 'T2001', 78, 1, '2025-07-10 09:00:00'),
('RA5117', 'C4007', 'S3007', 'T2003', NULL, 0, '2025-09-02 09:00:00'),
('RA5121', 'C4002', 'S3010', 'T2002', 72, 1, '2024-03-04 09:00:00'),
('RA5122', 'C4003', 'S3010', 'T2002', 69, 1, '2024-07-04 09:00:00'),
('RA5123', 'C4006', 'S3010', 'T2001', NULL, 0, '2025-09-03 09:00:00'),
('RA5131', 'C4001', 'S3011', 'T2001', 68, 1, '2024-03-05 09:00:00'),
('RA5132', 'C4002', 'S3011', 'T2002', 73, 1, '2024-03-05 09:05:00'),
('RA5133', 'C4003', 'S3011', 'T2002', 71, 1, '2024-07-05 09:00:00'),
('RA5134', 'C4008', 'S3011', 'T2003', NULL, 0, '2025-09-04 09:00:00')
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- 批量为新生 S3004~S3080 生成选课记录，让选课/成绩页面数据充足
-- 通识必修《大学英语》(C4009)：已录入成绩
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime)
WITH RECURSIVE seq (n) AS (
  SELECT 4
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 80
)
SELECT
  CONCAT('RB', LPAD(n, 4, '0')),
  'C4009',
  CONCAT('S', 3000 + n),
  'T2001',
  60 + MOD(n * 7, 40),
  1,
  DATE_ADD('2025-03-01 09:00:00', INTERVAL n MINUTE)
FROM seq
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- 通识选修《创新创业》(C4011)：本学期在读，未录入成绩
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime)
WITH RECURSIVE seq (n) AS (
  SELECT 4
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 80
)
SELECT
  CONCAT('RC', LPAD(n, 4, '0')),
  'C4011',
  CONCAT('S', 3000 + n),
  'T2003',
  NULL,
  0,
  DATE_ADD('2025-09-01 09:00:00', INTERVAL n MINUTE)
FROM seq
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

-- 为每位批量教师的专题课补充学生选课记录：一条已结课成绩、一条在修记录。
-- 这样教师端可以看到自己课程、学生列表、待录成绩和历史成绩。
INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime)
WITH RECURSIVE seq (n) AS (
  SELECT 4
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
  CONCAT('RT', LPAD(n, 4, '0')),
  CONCAT('C42', LPAD(n, 2, '0')),
  CONCAT('S', 3000 + n),
  CONCAT('T', 2000 + n),
  70 + MOD(n * 3, 26),
  1,
  DATE_ADD('2025-05-01 09:00:00', INTERVAL n MINUTE)
FROM seq
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime)
WITH RECURSIVE seq (n) AS (
  SELECT 4
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
  CONCAT('RU', LPAD(n, 4, '0')),
  CONCAT('C42', LPAD(n, 2, '0')),
  CONCAT('S', 3030 + n),
  CONCAT('T', 2000 + n),
  NULL,
  0,
  DATE_ADD('2025-09-01 10:00:00', INTERVAL n MINUTE)
FROM seq
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

-- ============ 演示数据：课程评价（仅针对已结课/已录入成绩的课程） ============
INSERT INTO tb_course_evaluation (id, course_id, student_id, teacher_id, rating, comment, anonymous, create_time) VALUES
('E7001', 'C4001', 'S3001', 'T2001', 5, '张老师讲得很细致，Java入门体验非常好', 0, '2024-07-01 10:00:00'),
('E7002', 'C4002', 'S3001', 'T2002', 4, '数据结构内容扎实，作业偏多', 0, '2024-07-01 10:05:00'),
('E7003', 'C4009', 'S3001', 'T2001', 4, '英语课氛围不错', 1, '2024-07-01 10:10:00'),
('E7004', 'C4001', 'S3002', 'T2001', 4, '基础讲解清晰，希望多一些实战案例', 1, '2024-07-02 09:00:00'),
('E7005', 'C4002', 'S3002', 'T2002', 3, '难度较大，节奏偏快', 0, '2024-07-02 09:05:00')
ON DUPLICATE KEY UPDATE
course_id = VALUES(course_id),
student_id = VALUES(student_id),
teacher_id = VALUES(teacher_id),
rating = VALUES(rating),
comment = VALUES(comment),
anonymous = VALUES(anonymous),
create_time = VALUES(create_time);

-- 高级课程与专题课的评教补充：评教必须依赖已结课且已录入成绩的选课记录。
INSERT INTO tb_course_evaluation (id, course_id, student_id, teacher_id, rating, comment, anonymous, create_time) VALUES
('EA7101', 'C4004', 'S3006', 'T2001', 4, '项目练习贴近实际，对后续架构课程帮助很大', 0, '2025-01-15 10:00:00'),
('EA7102', 'C4005', 'S3007', 'T2003', 5, '案例丰富，设计模式讲解清楚', 1, '2025-07-20 10:00:00'),
('EA7103', 'C4006', 'S3007', 'T2001', 4, '分布式系统内容扎实，实验难度适中', 0, '2025-07-20 10:05:00'),
('EA7104', 'C4003', 'S3010', 'T2002', 4, 'SQL 和事务部分讲得比较系统', 1, '2025-01-16 10:00:00')
ON DUPLICATE KEY UPDATE
course_id = VALUES(course_id),
student_id = VALUES(student_id),
teacher_id = VALUES(teacher_id),
rating = VALUES(rating),
comment = VALUES(comment),
anonymous = VALUES(anonymous),
create_time = VALUES(create_time);

INSERT INTO tb_course_evaluation (id, course_id, student_id, teacher_id, rating, comment, anonymous, create_time)
WITH RECURSIVE seq (n) AS (
  SELECT 4
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
  CONCAT('ET', LPAD(n, 4, '0')),
  CONCAT('C42', LPAD(n, 2, '0')),
  CONCAT('S', 3000 + n),
  CONCAT('T', 2000 + n),
  3 + MOD(n, 3),
  CONCAT('专题课内容完整，实践任务与课程目标匹配（T', 2000 + n, '）'),
  IF(MOD(n, 2) = 0, 1, 0),
  DATE_ADD('2025-07-25 10:00:00', INTERVAL n MINUTE)
FROM seq
ON DUPLICATE KEY UPDATE
course_id = VALUES(course_id),
student_id = VALUES(student_id),
teacher_id = VALUES(teacher_id),
rating = VALUES(rating),
comment = VALUES(comment),
anonymous = VALUES(anonymous),
create_time = VALUES(create_time);

-- ============ 种子数据依赖自检 ============
-- 下面结果的 issue_count 应全部为 0；若非 0，说明存在孤立数据或业务依赖不完整。
SELECT 'course_without_teacher' AS check_item, COUNT(*) AS issue_count
FROM tb_course c
LEFT JOIN tb_teacher t ON t.id = c.tid
WHERE c.tid IS NOT NULL AND t.id IS NULL
UNION ALL
SELECT 'selection_without_course', COUNT(*)
FROM tb_sct s
LEFT JOIN tb_course c ON c.id = s.courseid
WHERE c.id IS NULL
UNION ALL
SELECT 'selection_without_student', COUNT(*)
FROM tb_sct s
LEFT JOIN tb_student st ON st.id = s.studentId
WHERE st.id IS NULL
UNION ALL
SELECT 'selection_without_teacher', COUNT(*)
FROM tb_sct s
LEFT JOIN tb_teacher t ON t.id = s.teaid
WHERE s.teaid IS NOT NULL AND t.id IS NULL
UNION ALL
SELECT 'selection_teacher_mismatch', COUNT(*)
FROM tb_sct s
JOIN tb_course c ON c.id = s.courseid
WHERE s.teaid IS NOT NULL AND c.tid IS NOT NULL AND s.teaid <> c.tid
UNION ALL
SELECT 'prerequisite_without_course', COUNT(*)
FROM tb_course_prerequisite p
LEFT JOIN tb_course c ON c.id = p.course_id
LEFT JOIN tb_course pc ON pc.id = p.prerequisite_course_id
WHERE c.id IS NULL OR pc.id IS NULL
UNION ALL
SELECT 'mutex_without_course', COUNT(*)
FROM tb_course_mutex m
LEFT JOIN tb_course ca ON ca.id = m.course_id_a
LEFT JOIN tb_course cb ON cb.id = m.course_id_b
WHERE ca.id IS NULL OR cb.id IS NULL
UNION ALL
SELECT 'evaluation_without_parent', COUNT(*)
FROM tb_course_evaluation e
LEFT JOIN tb_course c ON c.id = e.course_id
LEFT JOIN tb_student st ON st.id = e.student_id
LEFT JOIN tb_teacher t ON t.id = e.teacher_id
WHERE c.id IS NULL OR st.id IS NULL OR t.id IS NULL
UNION ALL
SELECT 'evaluation_teacher_mismatch', COUNT(*)
FROM tb_course_evaluation e
JOIN tb_course c ON c.id = e.course_id
WHERE e.teacher_id IS NOT NULL AND c.tid IS NOT NULL AND e.teacher_id <> c.tid
UNION ALL
SELECT 'evaluation_without_graded_selection', COUNT(*)
FROM tb_course_evaluation e
LEFT JOIN tb_sct s
  ON s.courseid = e.course_id
 AND s.studentId = e.student_id
 AND s.graded = 1
WHERE s.id IS NULL
UNION ALL
SELECT 'seed_teacher_without_course', COUNT(*)
FROM tb_teacher t
LEFT JOIN tb_course c ON c.tid = t.id
WHERE t.id BETWEEN 'T2001' AND 'T2030' AND c.id IS NULL
UNION ALL
SELECT 'seed_course_without_selection', COUNT(*)
FROM tb_course c
LEFT JOIN tb_sct s ON s.courseid = c.id
WHERE (
    c.id BETWEEN 'C4001' AND 'C4012'
    OR c.id BETWEEN 'C4204' AND 'C4230'
  )
  AND s.id IS NULL
UNION ALL
SELECT 'seed_student_without_selection', COUNT(*)
FROM tb_student st
LEFT JOIN tb_sct s ON s.studentId = st.id
WHERE st.id BETWEEN 'S3001' AND 'S3080' AND s.id IS NULL;
