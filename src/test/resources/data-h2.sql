-- H2 兼容种子数据（测试用）
-- 所有演示账号初始密码均为 123456 (BCrypt哈希)

MERGE INTO tb_admin (id, username, password, name, tele) KEY(id) VALUES
('A1001', 'admin', '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q', '系统管理员', '13800000001');

MERGE INTO tb_teacher (id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender) KEY(id) VALUES
('T2001', 't_zhang', '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q', '2024001', '张若琳', '1987-09-12 00:00:00', '教授', '13810010001', 36, '女'),
('T2002', 't_li',    '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q', '2024002', '李振华', '1985-03-21 00:00:00', '副教授', '13810010002', 39, '男'),
('T2003', 't_sun',   '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q', '2024003', '孙启明', '1990-01-16 00:00:00', '讲师', '13810010003', 34, '男');

MERGE INTO tb_student (id, username, password, numb, sname, sdept, sbirthday, tele, email, ssex, age, smajor, sclass, grade, enrollment_year) KEY(id) VALUES
('S3001', 'stu_chen', '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q', '2023001', '陈知远', '信息工程学院', '2004-03-11 00:00:00', '13920010001', 'stu_chen@example.com', '男', 20, '软件工程', '软工2301', 2, 2023),
('S3002', 'stu_lin',  '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q', '2023002', '林晚晴', '信息工程学院', '2004-06-20 00:00:00', '13920010002', 'stu_lin@example.com',  '女', 20, '网络工程', '网工2301', 2, 2023),
('S3003', 'stu_qin',  '$2a$10$8e3/3cz9z0kg7NGotuPXy.fRUEr32fHqXyN6JvE9YHE.kO8JsRx7q', '2024001', '秦语桐', '数据科学学院', '2005-12-09 00:00:00', '13920010003', 'stu_qin@example.com',  '女', 18, '数据科学', '数科2401', 1, 2024);

MERGE INTO tb_course (id, name, score, numb, tid, jianjie, dept, max_students, time_slot, course_type, grade_limit) KEY(id) VALUES
('C4001', 'Java基础',       3.0, 'K23001',    'T2001', 'Java语言基础入门课程',           '信息工程学院', 60,  '周一第1-2节', '专业必修', 1),
('C4002', '数据结构',       4.0, 'K23002',    'T2002', '数据结构与算法基础',             '信息工程学院', 60,  '周二第3-4节', '专业必修', 1),
('C4003', '数据库原理',     3.0, 'K23003',    'T2002', '关系数据库理论与SQL',            '信息工程学院', 60,  '周三第1-2节', '专业必修', 2),
('C4004', 'Java Web开发',   3.5, 'K23004',    'T2001', 'Web应用开发实战',               '信息工程学院', 50,  '周四第3-4节', '专业必修', 2),
('C4005', '软件架构设计',   2.5, 'K23005',    'T2003', '软件系统架构与设计模式',         '信息工程学院', 40,  '周五第1-2节', '专业选修', 3),
('C4006', '分布式系统',     3.0, 'K23006',    'T2001', '分布式系统原理与实践',           '信息工程学院', 40,  '周一第5-6节', '专业选修', 3),
('C4007', '微服务架构',     3.0, 'K23007',    'T2003', '微服务设计与实现',              '信息工程学院', 35,  '周二第5-6节', '专业选修', 4),
('C4008', 'Python Web开发', 3.5, 'K23008',    'T2003', 'Python Web框架应用',            '信息工程学院', 50,  '周四第3-4节', '专业选修', 2),
('C4009', '大学英语',       2.0, 'K23009',    'T2001', '通识英语课程',                  '外国语学院', 80,  '周三第3-4节', '通识必修', NULL),
('C4010', '艺术鉴赏',       1.5, 'K23010',    'T2002', '艺术与美学入门',                '艺术学院',    100, '周五第5-6节', '通识选修', NULL),
('C4011', '创新创业',       1.5, 'K23011',    'T2003', '创业思维与实践',                '创新学院',    60,  '周三第7-8节', '通识选修', NULL),
('C4012', '音乐欣赏',       1.5, 'K23012',    'T2001', '音乐基础与鉴赏',                '艺术学院',    100, '周四第7-8节', '通识选修', NULL),
('C9001', 'Mantis重复选课测试', 1.0, 'BUG-DUP-001', 'T2001', '实验三第一轮缺陷复现课程', '质量审计实验', 30, '周六第3-4节', '缺陷复现', NULL),
('C9002', 'Mantis容量边界测试', 1.0, 'BUG-CAP-001', 'T2001', '实验三第二轮缺陷复现课程', '质量审计实验', 1,  '周六第5-6节', '缺陷复现', NULL);

MERGE INTO tb_course_prerequisite (id, course_id, prerequisite_course_id, min_score) KEY(id) VALUES
('P1001', 'C4003', 'C4002', 60),
('P1002', 'C4004', 'C4001', 60),
('P1003', 'C4005', 'C4004', 70),
('P1004', 'C4006', 'C4003', 60),
('P1005', 'C4007', 'C4006', 65);

MERGE INTO tb_course_mutex (id, course_id_a, course_id_b, reason) KEY(id) VALUES
('M2001', 'C4004', 'C4008', 'Java Web开发 与 Python Web开发 为同类型课程，只能选其一'),
('M2002', 'C4008', 'C4004', 'Python Web开发 与 Java Web开发 为同类型课程，只能选其一');

MERGE INTO tb_course_type_limit (id, course_type, max_courses, description) KEY(id) VALUES
('L3001', '通识选修', 3, '每学期最多选3门通识选修课'),
('L3002', '专业选修', 5, '每学期最多选5门专业选修课');

MERGE INTO tb_semester_credit_limit (id, min_gpa, max_gpa, max_credits, description) KEY(id) VALUES
('CL4001', 0.0, 2.0, 15.0, 'GPA低于2.0，学业预警，限制选课学分'),
('CL4002', 2.0, 3.0, 20.0, 'GPA 2.0-3.0，正常学分上限'),
('CL4003', 3.0, 3.5, 25.0, 'GPA 3.0-3.5，优秀学生可多选学分'),
('CL4004', 3.5, 5.0, 30.0, 'GPA 3.5以上，学霸特权，最高学分上限');

DELETE FROM tb_sct WHERE courseid IN ('C9001', 'C9002');

MERGE INTO tb_sct (id, courseid, studentId, teaid, score, graded, createtime) KEY(id) VALUES
('R5001', 'C4001', 'S3001', 'T2001', 85,   1, '2024-03-01 09:00:00'),
('R5002', 'C4002', 'S3001', 'T2002', 90,   1, '2024-03-01 10:00:00'),
('R5003', 'C4009', 'S3001', 'T2001', 78,   1, '2024-03-01 11:00:00'),
('R5004', 'C4003', 'S3001', 'T2002', NULL, 0, '2024-09-01 09:00:00'),
('R5005', 'C4004', 'S3001', 'T2001', NULL, 0, '2024-09-01 10:00:00'),
('R5006', 'C4010', 'S3001', 'T2002', NULL, 0, '2024-09-01 11:00:00'),
('R5007', 'C4001', 'S3002', 'T2001', 75,   1, '2024-03-01 09:00:00'),
('R5008', 'C4002', 'S3002', 'T2002', 65,   1, '2024-03-01 10:00:00'),
('R5009', 'C4009', 'S3002', 'T2001', 82,   1, '2024-03-01 11:00:00'),
('R5010', 'C4003', 'S3002', 'T2002', NULL, 0, '2024-09-01 09:00:00'),
('R5011', 'C4008', 'S3002', 'T2003', NULL, 0, '2024-09-01 10:00:00'),
('R5012', 'C4011', 'S3002', 'T2003', NULL, 0, '2024-09-01 11:00:00'),
('R5013', 'C4001', 'S3003', 'T2001', NULL, 0, '2024-09-01 09:00:00'),
('R5014', 'C4009', 'S3003', 'T2001', NULL, 0, '2024-09-01 10:00:00'),
('R5015', 'C4012', 'S3003', 'T2001', NULL, 0, '2024-09-01 11:00:00'),
('R9002', 'C9002', 'S3001', 'T2001', NULL, 0, '2026-06-01 09:00:00');

MERGE INTO tb_selection_window (id, action_type, name, start_time, end_time, enabled, description) KEY(id) VALUES
('W6001', 'SELECT', '演示选课窗口', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, '长期开放，便于本地功能测试'),
('W6002', 'DROP',   '演示退课窗口', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, '长期开放，便于本地功能测试');
