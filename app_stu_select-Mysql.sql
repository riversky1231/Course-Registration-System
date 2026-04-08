-- ----------------------------
-- TODO1 创建Mysql数据库和表，只需执行一次
-- ----------------------------
Create Database If Not Exists app_stu_select Character Set UTF8MB4;
use app_stu_select;

create table if not exists tb_admin (
    id varchar(32) comment '主键',
    username varchar(20) comment '用户名',
    password varchar(20) comment '密码',
    name varchar(18) comment '姓名',
    tele varchar(11) comment '电话',
    PRIMARY KEY (`id`)
)comment '管理员表';
-- 默认插入一条管理员 数据
insert ignore into tb_admin (id,username,password) values('1234567890','1','1');
create table if not exists tb_teacher (
    id varchar(32) comment '主键',
    username varchar(20) comment '用户名',
    password varchar(20) comment '密码',
    numb varchar(32) comment '教工号',
    tname varchar(18) comment '姓名',
    tbirthday datetime comment '出生日期',
    tposition varchar(255) comment '职位',
    ttel varchar(11) comment '电话',
    age int comment '年龄',
    gender varchar(10) comment '性别',
    PRIMARY KEY (`id`)
)comment '教师表';
create table if not exists tb_student (
    id varchar(32) comment '主键',
    username varchar(20) comment '用户名',
    password varchar(20) comment '密码',
    numb varchar(32) comment '学号',
    sname varchar(255) comment '姓名',
    sdept varchar(255) comment '学院',
    sbirthday datetime comment '出生日期',
    tele varchar(11) comment '电话',
    ssex varchar(10) comment '性别',
    age int comment '年龄',
    smajor varchar(255) comment '专业',
    sclass varchar(255) comment '班级',
    PRIMARY KEY (`id`)
)comment '学生表';
create table if not exists tb_course (
    id varchar(32) comment '主键',
    name varchar(18) comment '课程名称',
    score double comment '学分',
    numb varchar(32) comment '编号',
    tid varchar(32) comment '任课教师',
    jianjie varchar(255) comment '课程简介',
    PRIMARY KEY (`id`)
)comment '课程表';
create table if not exists tb_sct (
    id varchar(32) comment '主键',
    courseid varchar(32) comment '课程',
    studentId varchar(32) comment '学生',
    teaid varchar(32) comment '批改教师',
    score double comment '成绩',
    createtime datetime comment '学生选课时间',
    PRIMARY KEY (`id`)
)comment '学生选课表';




-- TODO 创建 随机数据
    
-- 管理员 插入语句
insert ignore into tb_admin values('9','myid','123','上官嘉','18956689501');
insert ignore into tb_admin values('10','ggg','123','童付付','13759519123');
insert ignore into tb_admin values('19','admin_demo','123','演示管理员','13611112222');
insert ignore into tb_admin values('20','audit_admin','123','质检管理员','13633334444');

    
-- 教师 插入语句
insert ignore into tb_teacher values('11','qqq','123','157022','洪金宝','2020-08-30 17:36:54','职位1024','15758840453','16','男');
insert ignore into tb_teacher values('12','gg','123','394331','洪小琼','2010-06-18 03:38:16','职位1025','15558888863','12','男');
insert ignore into tb_teacher values('21','t_zhang','123','201001','张若琳','1987-09-12 00:00:00','教授','13810010001','37','女');
insert ignore into tb_teacher values('22','t_li','123','201002','李振华','1985-03-21 00:00:00','副教授','13810010002','39','男');
insert ignore into tb_teacher values('23','t_sun','123','201003','孙启明','1990-01-16 00:00:00','讲师','13810010003','34','男');
insert ignore into tb_teacher values('24','t_chen','123','201004','陈清越','1988-07-08 00:00:00','副教授','13810010004','36','女');
insert ignore into tb_teacher values('25','t_wang','123','201005','王思哲','1986-12-09 00:00:00','教授','13810010005','38','男');
insert ignore into tb_teacher values('26','t_zhao','123','201006','赵南星','1992-05-11 00:00:00','讲师','13810010006','32','女');
insert ignore into tb_teacher values('27','t_he','123','201007','何嘉木','1989-11-02 00:00:00','实验中心主任','13810010007','35','男');
insert ignore into tb_teacher values('28','t_xu','123','201008','许安宁','1991-04-19 00:00:00','讲师','13810010008','33','女');

    
-- 学生 插入语句
insert ignore into tb_student values('13','qq','123','390522','吴亦凡','计算机','2018-06-13 09:22:11','17656889502','男','21','计算','班级1');
insert ignore into tb_student values('14','gg','123','022444','柳旭旭','计算机','2020-10-27 21:28:27','18956889502','男','11','软件','班级2');
insert ignore into tb_student values('31','stu_chen','123','2023001','陈知远','信息工程学院','2004-03-11 00:00:00','13920010001','男','20','软件工程','软工2301');
insert ignore into tb_student values('32','stu_lin','123','2023002','林晚晴','信息工程学院','2004-06-20 00:00:00','13920010002','女','20','网络工程','网工2301');
insert ignore into tb_student values('33','stu_qin','123','2023003','秦语桐','数据科学学院','2003-12-09 00:00:00','13920010003','女','21','数据科学','数科2301');
insert ignore into tb_student values('34','stu_peng','123','2023004','彭浩宇','人工智能学院','2004-01-18 00:00:00','13920010004','男','20','人工智能','智科2302');
insert ignore into tb_student values('35','stu_yao','123','2023005','姚一鸣','人工智能学院','2004-09-04 00:00:00','13920010005','男','20','机器人工程','机器人2301');
insert ignore into tb_student values('36','stu_gu','123','2023006','顾星澜','商学院','2003-11-23 00:00:00','13920010006','女','21','电子商务','电商2301');
insert ignore into tb_student values('37','stu_tan','123','2023007','谭乐川','信息工程学院','2004-07-29 00:00:00','13920010007','男','20','软件工程','软工2302');
insert ignore into tb_student values('38','stu_xie','123','2023008','谢听雨','设计学院','2004-08-15 00:00:00','13920010008','女','20','数字媒体','数媒2301');
insert ignore into tb_student values('39','stu_han','123','2023009','韩景尧','数据科学学院','2004-04-28 00:00:00','13920010009','男','20','大数据技术','大数2301');
insert ignore into tb_student values('40','stu_song','123','2023010','宋知夏','商学院','2003-10-10 00:00:00','13920010010','女','21','金融科技','金科2301');
insert ignore into tb_student values('41','stu_zhou','123','2023011','周墨','人工智能学院','2004-02-14 00:00:00','13920010011','男','20','人工智能','智科2301');
insert ignore into tb_student values('42','stu_fan','123','2023012','范可欣','设计学院','2004-05-17 00:00:00','13920010012','女','20','交互设计','交设2301');
insert ignore into tb_student values('43','stu_deng','123','2023013','邓思齐','信息工程学院','2004-01-05 00:00:00','13920010013','男','20','网络工程','网工2302');
insert ignore into tb_student values('44','stu_luo','123','2023014','罗青禾','数据科学学院','2003-09-26 00:00:00','13920010014','女','21','数据科学','数科2302');

    
-- 课程 插入语句
insert ignore into tb_course values('15','高数','35.2','980624','11','然绿意中鸟儿欢歌');
insert ignore into tb_course values('16','英语','58.3','967784','11','似乎在庆祝新的一');
insert ignore into tb_course values('51','Java Web 开发','3.5','C23001','21','覆盖 Servlet、Spring Boot、接口设计与部署流程');
insert ignore into tb_course values('52','数据库原理','3.0','C23002','22','关注关系模型、SQL 优化、事务与索引设计');
insert ignore into tb_course values('53','软件测试','2.5','C23003','23','包含黑盒、白盒、接口、性能和回归测试方法');
insert ignore into tb_course values('54','前端工程化','2.0','C23004','24','介绍 Vue、组件化、状态管理和工程化构建');
insert ignore into tb_course values('55','数据结构','4.0','C23005','25','强调线性表、树、图及算法复杂度分析');
insert ignore into tb_course values('56','操作系统','3.5','C23006','22','进程线程、存储管理、文件系统和并发控制');
insert ignore into tb_course values('57','人工智能导论','2.5','C23007','26','涵盖机器学习基础、推理、搜索和应用场景');
insert ignore into tb_course values('58','交互设计基础','2.0','C23008','28','聚焦用户体验、原型设计和信息架构');
insert ignore into tb_course values('59','网络安全','2.5','C23009','27','介绍鉴权、攻防思路、常见漏洞与防护实践');
insert ignore into tb_course values('60','软件项目管理','2.0','C23010','24','围绕需求、计划、风险与团队协作进行训练');
insert ignore into tb_course values('61','Python 数据分析','2.5','C23011','23','用数据处理案例训练分析、清洗与可视化能力');
insert ignore into tb_course values('62','机器学习实践','3.0','C23012','26','面向建模、特征工程和评估流程设计');
insert ignore into tb_course values('63','云原生应用开发','2.5','C23013','21','包含容器化、配置管理、部署和日志监控');
insert ignore into tb_course values('64','数字产品策划','2.0','C23014','28','训练产品调研、竞品分析和方案表达能力');

    
-- 学生选课 插入语句
insert ignore into tb_sct values('17','15','13','12','51','2025-06-03 09:19:38');
insert ignore into tb_sct values('18','16','13','12','66','2025-06-03 09:19:38');
insert ignore into tb_sct values('81','51','31','21','92','2025-06-04 08:10:00');
insert ignore into tb_sct values('82','52','31','22','88','2025-06-04 09:20:00');
insert ignore into tb_sct values('83','53','31','23','94','2025-06-04 10:30:00');
insert ignore into tb_sct values('84','54','32','24','91','2025-06-04 11:00:00');
insert ignore into tb_sct values('85','55','32','25','86','2025-06-05 08:15:00');
insert ignore into tb_sct values('86','57','33','26','95','2025-06-05 09:25:00');
insert ignore into tb_sct values('87','52','33','22','90','2025-06-05 10:35:00');
insert ignore into tb_sct values('88','59','34','27','89','2025-06-05 11:45:00');
insert ignore into tb_sct values('89','62','34','26','93','2025-06-06 08:05:00');
insert ignore into tb_sct values('90','57','35','26','87','2025-06-06 09:20:00');
insert ignore into tb_sct values('91','60','36','24','85','2025-06-06 10:20:00');
insert ignore into tb_sct values('92','64','36','28','90','2025-06-06 11:10:00');
insert ignore into tb_sct values('93','61','39','23','96','2025-06-07 08:50:00');
insert ignore into tb_sct values('94','53','39','23','93','2025-06-07 10:10:00');
insert ignore into tb_sct values('95','58','38','28','92','2025-06-07 11:30:00');
insert ignore into tb_sct values('96','63','37','21','88','2025-06-08 08:30:00');
insert ignore into tb_sct values('97','51','37','21','90','2025-06-08 09:40:00');
insert ignore into tb_sct values('98','56','40','22','84','2025-06-08 10:10:00');
insert ignore into tb_sct values('99','60','40','24','91','2025-06-08 11:20:00');
insert ignore into tb_sct values('100','62','41','26','94','2025-06-09 08:25:00');
insert ignore into tb_sct values('101','57','41','26','89','2025-06-09 09:35:00');
insert ignore into tb_sct values('102','58','42','28','95','2025-06-09 10:45:00');
insert ignore into tb_sct values('103','64','42','28','93','2025-06-09 11:55:00');
insert ignore into tb_sct values('104','59','43','27','87','2025-06-10 08:20:00');
insert ignore into tb_sct values('105','52','43','22','82','2025-06-10 09:15:00');
insert ignore into tb_sct values('106','61','44','23','91','2025-06-10 10:00:00');
insert ignore into tb_sct values('107','54','44','24','88','2025-06-10 11:35:00');
insert ignore into tb_sct values('108','63','14','21','79','2025-06-11 08:40:00');
insert ignore into tb_sct values('109','55','13','25','83','2025-06-11 09:50:00');
insert ignore into tb_sct values('110','56','32','22','85','2025-06-11 10:40:00');


-- TODO3 创建用户并授权
CREATE USER IF NOT EXISTS 'app_stu_select'@'%' IDENTIFIED BY 'app_stu_select';
ALTER USER 'app_stu_select'@'%' IDENTIFIED BY 'app_stu_select';
GRANT ALL ON app_stu_select.* TO 'app_stu_select'@'%';
FLUSH PRIVILEGES;

