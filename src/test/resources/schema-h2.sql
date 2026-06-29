-- H2 兼容建表脚本（测试用，替代 MySQL DDL）

CREATE TABLE IF NOT EXISTS tb_admin (
    id VARCHAR(32) PRIMARY KEY,
    username VARCHAR(20),
    password VARCHAR(100),
    name VARCHAR(18),
    tele VARCHAR(11)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tb_admin_username ON tb_admin (username);

CREATE TABLE IF NOT EXISTS tb_teacher (
    id VARCHAR(32) PRIMARY KEY,
    username VARCHAR(20),
    password VARCHAR(100),
    numb VARCHAR(32),
    tname VARCHAR(18),
    tbirthday TIMESTAMP,
    tposition VARCHAR(255),
    ttel VARCHAR(11),
    age INT,
    gender VARCHAR(10)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tb_teacher_username ON tb_teacher (username);

CREATE TABLE IF NOT EXISTS tb_student (
    id VARCHAR(32) PRIMARY KEY,
    username VARCHAR(20),
    password VARCHAR(100),
    numb VARCHAR(32),
    sname VARCHAR(255),
    sdept VARCHAR(255),
    sbirthday TIMESTAMP,
    tele VARCHAR(11),
    email VARCHAR(255),
    ssex VARCHAR(10),
    age INT,
    smajor VARCHAR(255),
    sclass VARCHAR(255),
    grade INT,
    enrollment_year INT
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tb_student_username ON tb_student (username);

CREATE TABLE IF NOT EXISTS tb_course (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(18),
    score DOUBLE,
    numb VARCHAR(32),
    tid VARCHAR(32),
    jianjie VARCHAR(255),
    dept VARCHAR(255),
    max_students INT DEFAULT 0,
    time_slot VARCHAR(64),
    course_type VARCHAR(64),
    grade_limit INT
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tb_course_numb ON tb_course (numb);

CREATE TABLE IF NOT EXISTS tb_sct (
    id VARCHAR(32) PRIMARY KEY,
    courseid VARCHAR(32),
    studentId VARCHAR(32),
    teaid VARCHAR(32),
    score DOUBLE,
    graded TINYINT DEFAULT 0,
    createtime TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tb_sct_course_student ON tb_sct (courseid, studentId);

CREATE INDEX IF NOT EXISTS idx_tb_sct_courseid ON tb_sct (courseid);

CREATE INDEX IF NOT EXISTS idx_tb_sct_studentid ON tb_sct (studentId);

CREATE INDEX IF NOT EXISTS idx_tb_sct_teaid ON tb_sct (teaid);

CREATE TABLE IF NOT EXISTS tb_admin_audit_log (
    id VARCHAR(32) PRIMARY KEY,
    admin_id VARCHAR(32),
    admin_username VARCHAR(20),
    action VARCHAR(16),
    target_type VARCHAR(32),
    target_id VARCHAR(32),
    target_name VARCHAR(255),
    detail VARCHAR(500),
    createtime TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_notification (
    id VARCHAR(32) PRIMARY KEY,
    recipient_id VARCHAR(32),
    recipient_role VARCHAR(20),
    recipient_name VARCHAR(255),
    channel VARCHAR(16),
    status VARCHAR(16),
    title VARCHAR(120),
    content VARCHAR(500),
    contact VARCHAR(255),
    result_message VARCHAR(255),
    createtime TIMESTAMP,
    senttime TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_selection_window (
    id VARCHAR(32) PRIMARY KEY,
    action_type VARCHAR(16),
    name VARCHAR(64),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    enabled TINYINT DEFAULT 1,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tb_course_prerequisite (
    id VARCHAR(32) PRIMARY KEY,
    course_id VARCHAR(32) NOT NULL,
    prerequisite_course_id VARCHAR(32) NOT NULL,
    min_score DOUBLE DEFAULT 60
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_course_prerequisite ON tb_course_prerequisite (
    course_id,
    prerequisite_course_id
);

CREATE INDEX IF NOT EXISTS idx_cp_course_id ON tb_course_prerequisite (course_id);

CREATE INDEX IF NOT EXISTS idx_cp_prerequisite_course_id ON tb_course_prerequisite (prerequisite_course_id);

CREATE TABLE IF NOT EXISTS tb_course_mutex (
    id VARCHAR(32) PRIMARY KEY,
    course_id_a VARCHAR(32) NOT NULL,
    course_id_b VARCHAR(32) NOT NULL,
    reason VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_course_mutex ON tb_course_mutex (course_id_a, course_id_b);

CREATE INDEX IF NOT EXISTS idx_cm_course_id_a ON tb_course_mutex (course_id_a);

CREATE INDEX IF NOT EXISTS idx_cm_course_id_b ON tb_course_mutex (course_id_b);

CREATE TABLE IF NOT EXISTS tb_course_type_limit (
    id VARCHAR(32) PRIMARY KEY,
    course_type VARCHAR(64) NOT NULL,
    max_courses INT NOT NULL,
    description VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_course_type ON tb_course_type_limit (course_type);

CREATE TABLE IF NOT EXISTS tb_semester_credit_limit (
    id VARCHAR(32) PRIMARY KEY,
    min_gpa DOUBLE NOT NULL,
    max_gpa DOUBLE NOT NULL,
    max_credits DOUBLE NOT NULL,
    description VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_gpa_range ON tb_semester_credit_limit (min_gpa, max_gpa);

CREATE TABLE IF NOT EXISTS tb_course_evaluation (
    id VARCHAR(32) PRIMARY KEY,
    course_id VARCHAR(32) NOT NULL,
    student_id VARCHAR(32) NOT NULL,
    teacher_id VARCHAR(32),
    rating INT NOT NULL,
    comment VARCHAR(500),
    anonymous TINYINT DEFAULT 0,
    create_time TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_course_evaluation ON tb_course_evaluation (course_id, student_id);

CREATE INDEX IF NOT EXISTS idx_ce_course_id ON tb_course_evaluation (course_id);

CREATE INDEX IF NOT EXISTS idx_ce_student_id ON tb_course_evaluation (student_id);

CREATE INDEX IF NOT EXISTS idx_ce_teacher_id ON tb_course_evaluation (teacher_id);