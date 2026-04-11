-- 项目唯一数据库脚本。
-- 团队协作时只维护这一份；不要再新增 schema.sql / data.sql。
-- 推荐用法：
-- 1. 在 MySQL 客户端中直接执行整份脚本；
-- 2. 如果你没有建库权限，先手工创建数据库并切到 app_stu_select，再从建表语句开始执行。

CREATE DATABASE IF NOT EXISTS app_stu_select CHARACTER SET utf8mb4;
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

-- 所有演示账号初始密码均为 123456，但数据库中保存的是 BCrypt 哈希值。
SET @seed_password_hash = '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q';

INSERT INTO tb_admin (id, username, password, name, tele) VALUES
('A1001', 'admin', @seed_password_hash, '系统管理员', '13800000001'),
('A1002', 'audit_admin', @seed_password_hash, '审计管理员', '13800000002')
ON DUPLICATE KEY UPDATE
username = VALUES(username),
password = VALUES(password),
name = VALUES(name),
tele = VALUES(tele);

INSERT INTO tb_teacher (id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender) VALUES
('T2001', 't_zhang', @seed_password_hash, '2024001', '张若琳', '1987-09-12 00:00:00', '教授', '13810010001', 37, '女'),
('T2002', 't_li', @seed_password_hash, '2024002', '李振华', '1985-03-21 00:00:00', '副教授', '13810010002', 39, '男'),
('T2003', 't_sun', @seed_password_hash, '2024003', '孙启明', '1990-01-16 00:00:00', '讲师', '13810010003', 34, '男'),
('T2004', 't_chen', @seed_password_hash, '2024004', '陈清越', '1988-07-08 00:00:00', '副教授', '13810010004', 36, '女'),
('T2005', 't_wang', @seed_password_hash, '2024005', '王思哲', '1986-12-09 00:00:00', '教授', '13810010005', 38, '男'),
('T2006', 't_zhao', @seed_password_hash, '2024006', '赵南星', '1992-05-11 00:00:00', '讲师', '13810010006', 32, '女'),
('T2007', 't_he', @seed_password_hash, '2024007', '何嘉木', '1989-11-02 00:00:00', '实验中心主任', '13810010007', 35, '男'),
('T2008', 't_xu', @seed_password_hash, '2024008', '许安宁', '1991-04-19 00:00:00', '讲师', '13810010008', 33, '女'),
('T2009', 't_luo', @seed_password_hash, '2024009', '罗清衡', '1984-08-03 00:00:00', '副教授', '13810010009', 40, '男'),
('T2010', 't_deng', @seed_password_hash, '2024010', '邓语禾', '1993-02-26 00:00:00', '讲师', '13810010010', 31, '女'),
('T2011', 't_ma', @seed_password_hash, '2024011', '马汇宁', '1986-06-14 00:00:00', '教授', '13810010011', 38, '男'),
('T2012', 't_guo', @seed_password_hash, '2024012', '郭临川', '1988-10-22 00:00:00', '副教授', '13810010012', 36, '男')
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

INSERT INTO tb_student (id, username, password, numb, sname, sdept, sbirthday, tele, email, ssex, age, smajor, sclass) VALUES
('S3001', 'stu_chen', @seed_password_hash, '2023001', '陈知远', '信息工程学院', '2004-03-11 00:00:00', '13920010001', 'stu_chen@example.com', '男', 20, '软件工程', '软工2301'),
('S3002', 'stu_lin', @seed_password_hash, '2023002', '林晚晴', '信息工程学院', '2004-06-20 00:00:00', '13920010002', 'stu_lin@example.com', '女', 20, '网络工程', '网工2301'),
('S3003', 'stu_qin', @seed_password_hash, '2023003', '秦语桐', '数据科学学院', '2003-12-09 00:00:00', '13920010003', 'stu_qin@example.com', '女', 21, '数据科学', '数科2301'),
('S3004', 'stu_peng', @seed_password_hash, '2023004', '彭浩宇', '人工智能学院', '2004-01-18 00:00:00', '13920010004', 'stu_peng@example.com', '男', 20, '人工智能', '智科2302'),
('S3005', 'stu_yao', @seed_password_hash, '2023005', '姚一鸣', '人工智能学院', '2004-09-04 00:00:00', '13920010005', 'stu_yao@example.com', '男', 20, '机器人工程', '机器人2301'),
('S3006', 'stu_gu', @seed_password_hash, '2023006', '顾星澜', '商学院', '2003-11-23 00:00:00', '13920010006', 'stu_gu@example.com', '女', 21, '电子商务', '电商2301'),
('S3007', 'stu_tan', @seed_password_hash, '2023007', '谭乐川', '信息工程学院', '2004-07-29 00:00:00', '13920010007', 'stu_tan@example.com', '男', 20, '软件工程', '软工2302'),
('S3008', 'stu_xie', @seed_password_hash, '2023008', '谢听雨', '设计学院', '2004-08-15 00:00:00', '13920010008', 'stu_xie@example.com', '女', 20, '数字媒体', '数媒2301'),
('S3009', 'stu_han', @seed_password_hash, '2023009', '韩景尧', '数据科学学院', '2004-04-28 00:00:00', '13920010009', 'stu_han@example.com', '男', 20, '大数据技术', '大数2301'),
('S3010', 'stu_song', @seed_password_hash, '2023010', '宋知夏', '商学院', '2003-10-10 00:00:00', '13920010010', 'stu_song@example.com', '女', 21, '金融科技', '金科2301'),
('S3011', 'stu_zhou', @seed_password_hash, '2023011', '周墨', '人工智能学院', '2004-02-14 00:00:00', '13920010011', 'stu_zhou@example.com', '男', 20, '人工智能', '智科2301'),
('S3012', 'stu_fan', @seed_password_hash, '2023012', '范可欣', '设计学院', '2004-05-17 00:00:00', '13920010012', 'stu_fan@example.com', '女', 20, '交互设计', '交设2301'),
('S3013', 'stu_cao', @seed_password_hash, '2023013', '曹予安', '信息工程学院', '2004-01-09 00:00:00', '13920010013', 'stu_cao@example.com', '男', 20, '网络工程', '网工2302'),
('S3014', 'stu_shen', @seed_password_hash, '2023014', '沈沐言', '数据科学学院', '2004-03-26 00:00:00', '13920010014', 'stu_shen@example.com', '女', 20, '数据科学', '数科2302'),
('S3015', 'stu_liu', @seed_password_hash, '2023015', '刘承泽', '人工智能学院', '2004-05-06 00:00:00', '13920010015', 'stu_liu@example.com', '男', 20, '智能科学', '智科2303'),
('S3016', 'stu_yuan', @seed_password_hash, '2023016', '袁可宁', '设计学院', '2004-07-12 00:00:00', '13920010016', 'stu_yuan@example.com', '女', 20, '视觉传达', '视传2301'),
('S3017', 'stu_wei', @seed_password_hash, '2023017', '魏清妍', '商学院', '2004-04-09 00:00:00', '13920010017', 'stu_wei@example.com', '女', 20, '市场营销', '营销2301'),
('S3018', 'stu_du', @seed_password_hash, '2023018', '杜柏川', '人工智能学院', '2004-08-18 00:00:00', '13920010018', 'stu_du@example.com', '男', 20, '机器人工程', '机器人2302'),
('S3019', 'stu_kong', @seed_password_hash, '2023019', '孔书瑶', '数据科学学院', '2004-09-28 00:00:00', '13920010019', 'stu_kong@example.com', '女', 20, '大数据技术', '大数2302'),
('S3020', 'stu_mo', @seed_password_hash, '2023020', '莫嘉树', '信息工程学院', '2004-02-02 00:00:00', '13920010020', 'stu_mo@example.com', '男', 20, '软件工程', '软工2303'),
('S3021', 'stu_pei', @seed_password_hash, '2023021', '裴星语', '设计学院', '2004-11-03 00:00:00', '13920010021', 'stu_pei@example.com', '女', 19, '数字媒体', '数媒2302'),
('S3022', 'stu_huo', @seed_password_hash, '2023022', '霍闻川', '商学院', '2004-06-30 00:00:00', '13920010022', 'stu_huo@example.com', '男', 20, '金融科技', '金科2302'),
('S3023', 'stu_lan', @seed_password_hash, '2023023', '蓝知意', '人工智能学院', '2004-12-21 00:00:00', '13920010023', 'stu_lan@example.com', '女', 19, '人工智能', '智科2304'),
('S3024', 'stu_jiang', @seed_password_hash, '2023024', '江叙白', '信息工程学院', '2004-10-18 00:00:00', '13920010024', 'stu_jiang@example.com', '男', 19, '信息安全', '信安2301')
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
sclass = VALUES(sclass);

INSERT INTO tb_course (id, name, score, numb, tid, jianjie, dept, max_students, time_slot) VALUES
('C4001', 'Java Web开发', 3.5, 'K23001', 'T2001', '覆盖 Web 应用开发、接口设计、部署发布与项目协作。', '信息工程学院', 60, '周一第3-4节'),
('C4002', '数据库原理', 3.0, 'K23002', 'T2002', '关注关系模型、SQL 优化、事务处理与索引设计。', '数据科学学院', 60, '周二第1-2节'),
('C4003', '软件架构设计', 2.5, 'K23003', 'T2003', '围绕模块划分、系统分层与可维护性展开实践。', '信息工程学院', 40, '周一第1-2节'),
('C4004', '前端交互设计', 2.0, 'K23004', 'T2004', '聚焦页面结构、交互流程、组件协作和体验表达。', '设计学院', 40, '周三第3-4节'),
('C4005', '数据结构', 4.0, 'K23005', 'T2005', '强调线性表、树、图与算法复杂度分析。', '信息工程学院', 60, '周二第3-4节'),
('C4006', '操作系统', 3.5, 'K23006', 'T2002', '包含进程线程、存储管理、文件系统与并发控制。', '信息工程学院', 60, '周三第1-2节'),
('C4007', '人工智能导论', 2.5, 'K23007', 'T2006', '介绍搜索、推理、机器学习基础和典型应用场景。', '人工智能学院', 40, '周四第3-4节'),
('C4008', '网络安全基础', 2.5, 'K23008', 'T2007', '围绕认证授权、常见漏洞、防护策略与安全规范。', '信息工程学院', 40, '周五第1-2节'),
('C4009', '云原生应用开发', 2.5, 'K23009', 'T2001', '涵盖容器化部署、配置管理、日志监控和服务协作。', '信息工程学院', 40, '周四第1-2节'),
('C4010', '数字产品策划', 2.0, 'K23010', 'T2008', '训练需求分析、竞品研究、方案表达与项目复盘。', '商学院', 50, '周五第3-4节'),
('C4011', '创新创业实践', 1.5, 'K23011', '', '为跨学院项目预留课程，等待学院完成任课教师安排。', '创新创业学院', 30, '周三第5-6节'),
('C4012', 'Python数据分析', 3.0, 'K23012', 'T2009', '围绕数据清洗、统计建模和分析报告展开项目训练。', '数据科学学院', 60, '周一第5-6节'),
('C4013', '分布式系统', 3.0, 'K23013', 'T2001', '学习服务拆分、注册发现、一致性与弹性治理。', '信息工程学院', 50, '周四第5-6节'),
('C4014', '机器学习基础', 3.5, 'K23014', 'T2010', '覆盖监督学习、模型评估、特征工程与实验复盘。', '人工智能学院', 50, '周三第7-8节'),
('C4015', 'UI设计方法', 2.0, 'K23015', 'T2004', '通过案例拆解界面信息层级、交互逻辑与视觉规范。', '设计学院', 45, '周二第5-6节'),
('C4016', '金融数据分析', 2.5, 'K23016', 'T2011', '结合业务指标、数据仪表板和风险判断进行分析实践。', '商学院', 50, '周一第7-8节'),
('C4017', '计算机图形学', 3.0, 'K23017', 'T2008', '讲解图形渲染、坐标变换、动画基础与可视化表达。', '设计学院', 45, '周二第7-8节'),
('C4018', '信息检索技术', 2.5, 'K23018', 'T2007', '介绍检索模型、索引结构、排序机制与搜索体验设计。', '信息工程学院', 45, '周五第5-6节'),
('C4019', '微服务架构', 3.0, 'K23019', 'T2003', '聚焦服务拆分、接口治理、容错策略与可观测性建设。', '信息工程学院', 45, '周三第9-10节'),
('C4020', '数据可视化', 2.5, 'K23020', 'T2012', '训练图表设计、视觉编码和数据叙事表达能力。', '数据科学学院', 45, '周四第7-8节')
ON DUPLICATE KEY UPDATE
name = VALUES(name),
score = VALUES(score),
numb = VALUES(numb),
tid = VALUES(tid),
jianjie = VALUES(jianjie),
dept = VALUES(dept),
max_students = VALUES(max_students),
time_slot = VALUES(time_slot);

INSERT INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) VALUES
('R5001', 'C4001', 'S3001', 'T2001', 92, 1, '2026-03-01 09:00:00'),
('R5002', 'C4002', 'S3001', 'T2002', 88, 1, '2026-03-01 10:30:00'),
('R5003', 'C4003', 'S3001', 'T2003', NULL, 0, '2026-03-02 08:40:00'),
('R5004', 'C4004', 'S3002', 'T2004', 91, 1, '2026-03-02 09:20:00'),
('R5005', 'C4005', 'S3002', 'T2005', 86, 1, '2026-03-03 08:15:00'),
('R5006', 'C4007', 'S3003', 'T2006', 95, 1, '2026-03-03 09:25:00'),
('R5007', 'C4002', 'S3003', 'T2002', 90, 1, '2026-03-03 10:35:00'),
('R5008', 'C4008', 'S3004', 'T2007', 89, 1, '2026-03-04 11:10:00'),
('R5009', 'C4007', 'S3004', 'T2006', 93, 1, '2026-03-04 14:05:00'),
('R5010', 'C4007', 'S3005', 'T2006', 87, 1, '2026-03-05 08:45:00'),
('R5011', 'C4010', 'S3006', 'T2008', 90, 1, '2026-03-05 09:40:00'),
('R5012', 'C4009', 'S3007', 'T2001', 88, 1, '2026-03-05 10:50:00'),
('R5013', 'C4001', 'S3007', 'T2001', 90, 1, '2026-03-06 08:20:00'),
('R5014', 'C4006', 'S3010', 'T2002', 84, 1, '2026-03-06 10:00:00'),
('R5015', 'C4003', 'S3009', 'T2003', 93, 1, '2026-03-06 11:15:00'),
('R5016', 'C4004', 'S3012', 'T2004', NULL, 0, '2026-03-07 08:55:00'),
('R5017', 'C4010', 'S3012', 'T2008', 95, 1, '2026-03-07 09:50:00'),
('R5018', 'C4008', 'S3011', 'T2007', 87, 1, '2026-03-07 10:40:00'),
('R5019', 'C4005', 'S3008', 'T2005', 92, 1, '2026-03-08 08:30:00'),
('R5020', 'C4009', 'S3006', 'T2001', NULL, 0, '2026-03-08 09:35:00'),
('R5021', 'C4006', 'S3002', 'T2002', 85, 1, '2026-03-08 10:40:00'),
('R5022', 'C4002', 'S3011', 'T2002', 89, 1, '2026-03-08 11:30:00'),
('R5023', 'C4013', 'S3001', 'T2001', 94, 1, '2026-03-09 08:20:00'),
('R5024', 'C4012', 'S3002', 'T2009', NULL, 0, '2026-03-09 09:10:00'),
('R5025', 'C4015', 'S3002', 'T2004', 93, 1, '2026-03-09 10:00:00'),
('R5026', 'C4012', 'S3003', 'T2009', 91, 1, '2026-03-09 11:00:00'),
('R5027', 'C4020', 'S3003', 'T2012', 94, 1, '2026-03-09 14:15:00'),
('R5028', 'C4014', 'S3004', 'T2010', NULL, 0, '2026-03-10 08:35:00'),
('R5029', 'C4018', 'S3004', 'T2007', 88, 1, '2026-03-10 09:25:00'),
('R5030', 'C4016', 'S3005', 'T2011', 90, 1, '2026-03-10 10:20:00'),
('R5031', 'C4009', 'S3005', 'T2001', NULL, 0, '2026-03-10 11:05:00'),
('R5032', 'C4016', 'S3006', 'T2011', 92, 1, '2026-03-10 13:10:00'),
('R5033', 'C4019', 'S3007', 'T2003', 89, 1, '2026-03-10 14:00:00'),
('R5034', 'C4008', 'S3007', 'T2007', 91, 1, '2026-03-10 15:00:00'),
('R5035', 'C4017', 'S3008', 'T2008', 94, 1, '2026-03-11 08:40:00'),
('R5036', 'C4004', 'S3008', 'T2004', 90, 1, '2026-03-11 09:35:00'),
('R5037', 'C4012', 'S3009', 'T2009', 90, 1, '2026-03-11 10:30:00'),
('R5038', 'C4020', 'S3009', 'T2012', NULL, 0, '2026-03-11 11:20:00'),
('R5039', 'C4016', 'S3010', 'T2011', 88, 1, '2026-03-11 14:00:00'),
('R5040', 'C4018', 'S3010', 'T2007', 92, 1, '2026-03-11 14:50:00'),
('R5041', 'C4014', 'S3011', 'T2010', 93, 1, '2026-03-12 08:25:00'),
('R5042', 'C4013', 'S3011', 'T2001', 91, 1, '2026-03-12 09:15:00'),
('R5043', 'C4015', 'S3012', 'T2004', 94, 1, '2026-03-12 10:05:00'),
('R5044', 'C4020', 'S3012', 'T2012', NULL, 0, '2026-03-12 10:55:00'),
('R5045', 'C4013', 'S3013', 'T2001', 87, 1, '2026-03-12 13:10:00'),
('R5046', 'C4018', 'S3013', 'T2007', 85, 1, '2026-03-12 14:05:00'),
('R5047', 'C4012', 'S3014', 'T2009', 95, 1, '2026-03-12 15:00:00'),
('R5048', 'C4020', 'S3014', 'T2012', 93, 1, '2026-03-12 15:55:00'),
('R5049', 'C4014', 'S3015', 'T2010', 89, 1, '2026-03-13 08:20:00'),
('R5050', 'C4007', 'S3015', 'T2006', 92, 1, '2026-03-13 09:15:00'),
('R5051', 'C4015', 'S3016', 'T2004', 96, 1, '2026-03-13 10:05:00'),
('R5052', 'C4017', 'S3016', 'T2008', 90, 1, '2026-03-13 10:55:00'),
('R5053', 'C4016', 'S3017', 'T2011', 91, 1, '2026-03-13 13:20:00'),
('R5054', 'C4010', 'S3017', 'T2008', 89, 1, '2026-03-13 14:10:00'),
('R5055', 'C4014', 'S3018', 'T2010', NULL, 0, '2026-03-13 15:05:00'),
('R5056', 'C4007', 'S3018', 'T2006', 90, 1, '2026-03-13 15:55:00'),
('R5057', 'C4012', 'S3019', 'T2009', 88, 1, '2026-03-14 08:35:00'),
('R5058', 'C4002', 'S3019', 'T2002', 90, 1, '2026-03-14 09:25:00'),
('R5059', 'C4019', 'S3020', 'T2003', 92, 1, '2026-03-14 10:15:00'),
('R5060', 'C4005', 'S3020', 'T2005', 94, 1, '2026-03-14 11:05:00'),
('R5061', 'C4017', 'S3021', 'T2008', 95, 1, '2026-03-14 13:30:00'),
('R5062', 'C4004', 'S3021', 'T2004', 93, 1, '2026-03-14 14:20:00'),
('R5063', 'C4016', 'S3022', 'T2011', 87, 1, '2026-03-14 15:10:00'),
('R5064', 'C4010', 'S3022', 'T2008', NULL, 0, '2026-03-14 16:00:00'),
('R5065', 'C4014', 'S3023', 'T2010', 94, 1, '2026-03-15 08:25:00'),
('R5066', 'C4007', 'S3023', 'T2006', 96, 1, '2026-03-15 09:15:00'),
('R5067', 'C4013', 'S3024', 'T2001', 90, 1, '2026-03-15 10:05:00'),
('R5068', 'C4008', 'S3024', 'T2007', 92, 1, '2026-03-15 10:55:00')
ON DUPLICATE KEY UPDATE
courseid = VALUES(courseid),
studentId = VALUES(studentId),
teaid = VALUES(teaid),
score = VALUES(score),
graded = VALUES(graded),
createtime = VALUES(createtime);

INSERT INTO tb_selection_window (id, action_type, name, start_time, end_time, enabled, description) VALUES
('W7001', 'SELECT', '2026春季学期选课窗口', '2026-02-20 08:00:00', '2026-05-31 23:59:59', 1, '学生在窗口期内可发起选课'),
('W7002', 'DROP', '2026春季学期退课窗口', '2026-02-20 08:00:00', '2026-05-31 23:59:59', 1, '学生在窗口期内可办理退课'),
('W7003', 'SELECT', '2025秋季学期选课窗口', '2025-08-20 08:00:00', '2025-09-10 23:59:59', 1, '历史学期窗口，用于演示窗口列表'),
('W7004', 'DROP', '2025秋季学期退课窗口', '2025-08-20 08:00:00', '2025-10-01 23:59:59', 0, '历史窗口已停用，用于演示启停状态')
ON DUPLICATE KEY UPDATE
action_type = VALUES(action_type),
name = VALUES(name),
start_time = VALUES(start_time),
end_time = VALUES(end_time),
enabled = VALUES(enabled),
description = VALUES(description);

INSERT INTO tb_notification (
id, recipient_id, recipient_role, recipient_name, channel, status, title, content, contact, result_message, createtime, senttime
) VALUES
('N8001', 'S3001', 'student', '陈知远', 'SYSTEM', 'SENT', '成绩发布通知', '《Java Web开发》成绩已发布，当前成绩为 92 分。', '', '站内通知已生成', '2026-03-10 09:00:00', '2026-03-10 09:00:00'),
('N8002', 'S3001', 'student', '陈知远', 'SYSTEM', 'SENT', '成绩发布通知', '《Java Web开发》成绩已发布，当前成绩为 92 分。', '', '站内通知已生成', '2026-03-10 09:00:01', '2026-03-10 09:00:01'),
('N8003', 'S3002', 'student', '林晚晴', 'SYSTEM', 'SENT', '选课成功通知', '你已成功选上《数据结构》，上课时间：周二第3-4节。', '', '站内通知已生成', '2026-03-03 08:16:00', '2026-03-03 08:16:00'),
('N8004', 'S3002', 'student', '林晚晴', 'SYSTEM', 'SENT', '选课成功通知', '你已成功选上《数据结构》，上课时间：周二第3-4节。', '', '站内通知已生成', '2026-03-03 08:16:01', '2026-03-03 08:16:01'),
('N8005', 'S3003', 'student', '秦语桐', 'SYSTEM', 'SENT', '成绩发布通知', '《Python数据分析》成绩已发布，当前成绩为 91 分。', '', '站内通知已生成', '2026-03-12 09:30:00', '2026-03-12 09:30:00'),
('N8006', 'S3004', 'student', '彭浩宇', 'SYSTEM', 'SENT', '待录成绩提醒', '《机器学习基础》成绩尚未录入，请关注后续更新。', '', '站内通知已生成', '2026-03-12 10:30:00', '2026-03-12 10:30:00'),
('N8007', 'S3014', 'student', '沈沐言', 'SYSTEM', 'SENT', '成绩发布通知', '《数据可视化》成绩已发布，当前成绩为 93 分。', '', '站内通知已生成', '2026-03-13 15:10:00', '2026-03-13 15:10:00'),
('N8008', 'S3023', 'student', '蓝知意', 'SYSTEM', 'SENT', '成绩发布通知', '《人工智能导论》成绩已发布，当前成绩为 96 分。', '', '站内通知已生成', '2026-03-15 11:20:00', '2026-03-15 11:20:00'),
('N8009', 'T2001', 'teacher', '张若琳', 'SYSTEM', 'SENT', '成绩录入提醒', '你负责的课程仍有待录成绩记录，请及时处理。', '', '站内通知已生成', '2026-03-15 12:00:00', '2026-03-15 12:00:00'),
('N8010', 'A1001', 'admin', '系统管理员', 'SYSTEM', 'SENT', '窗口状态提醒', '2026 春季学期选课窗口正在开放中，请关注容量与冲突情况。', '', '站内通知已生成', '2026-03-15 12:10:00', '2026-03-15 12:10:00')
ON DUPLICATE KEY UPDATE
recipient_id = VALUES(recipient_id),
recipient_role = VALUES(recipient_role),
recipient_name = VALUES(recipient_name),
channel = VALUES(channel),
status = VALUES(status),
title = VALUES(title),
content = VALUES(content),
contact = VALUES(contact),
result_message = VALUES(result_message),
createtime = VALUES(createtime),
senttime = VALUES(senttime);

INSERT INTO tb_admin_audit_log (
id, admin_id, admin_username, action, target_type, target_id, target_name, detail, createtime
) VALUES
('L9001', 'A1001', 'admin', '新增', 'teacher', 'T2009', '罗清衡', '新增教师档案并补充教工号与联系方式。', '2026-02-22 09:10:00'),
('L9002', 'A1001', 'admin', '新增', 'course', 'C4012', 'Python数据分析', '新增数据科学学院课程并配置授课教师与时间段。', '2026-02-22 09:25:00'),
('L9003', 'A1002', 'audit_admin', '编辑', 'selectionWindow', 'W7001', '2026春季学期选课窗口', '调整选课窗口说明，明确学生可在窗口期内发起选课。', '2026-02-23 10:00:00'),
('L9004', 'A1001', 'admin', '新增', 'student', 'S3014', '沈沐言', '新增学生档案并维护学院、专业与班级信息。', '2026-02-24 11:20:00'),
('L9005', 'A1002', 'audit_admin', '编辑', 'course', 'C4019', '微服务架构', '更新课程简介与容量上限，补充可观测性建设说明。', '2026-02-25 14:40:00'),
('L9006', 'A1001', 'admin', '编辑', 'teacher', 'T2010', '邓语禾', '更新教师职位信息并同步联系电话。', '2026-02-26 15:15:00'),
('L9007', 'A1001', 'admin', '新增', 'course', 'C4020', '数据可视化', '新增跨学院课程，用于扩展学生成绩查询样本。', '2026-02-27 09:45:00'),
('L9008', 'A1002', 'audit_admin', '删除', 'selectionWindow', 'W7004', '2025秋季学期退课窗口', '停用历史退课窗口，保留记录用于审计追踪。', '2026-02-28 16:30:00')
ON DUPLICATE KEY UPDATE
admin_id = VALUES(admin_id),
admin_username = VALUES(admin_username),
action = VALUES(action),
target_type = VALUES(target_type),
target_id = VALUES(target_id),
target_name = VALUES(target_name),
detail = VALUES(detail),
createtime = VALUES(createtime);
