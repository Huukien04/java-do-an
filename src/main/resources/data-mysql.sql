-- ============================================================
-- 01_create_database.sql
-- MYSQL / MARIADB
-- TOPIC MANAGEMENT SYSTEM
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS final_results;
DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS council_topics;
DROP TABLE IF EXISTS council_members;
DROP TABLE IF EXISTS review_councils;
DROP TABLE IF EXISTS topic_registrations;
DROP TABLE IF EXISTS topic_advisors;
DROP TABLE IF EXISTS topics;
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS student_groups;
DROP TABLE IF EXISTS registration_periods;
DROP TABLE IF EXISTS teachers;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- 1. ROLES
-- ============================================================

CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 2. USERS
-- ============================================================

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email),

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 3. DEPARTMENTS
-- ============================================================

CREATE TABLE departments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_departments_code (code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 4. STUDENTS
-- ============================================================

CREATE TABLE students (
    id BIGINT NOT NULL,
    student_code VARCHAR(20) NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    academic_year VARCHAR(20) DEFAULT NULL,
    department_id BIGINT NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_students_code (student_code),

    CONSTRAINT fk_students_user
        FOREIGN KEY (id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_students_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 5. TEACHERS
-- ============================================================

CREATE TABLE teachers (
    id BIGINT NOT NULL,
    teacher_code VARCHAR(20) NOT NULL,
    academic_rank VARCHAR(50) DEFAULT NULL,
    degree VARCHAR(50) DEFAULT NULL,
    department_id BIGINT NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_teachers_code (teacher_code),

    CONSTRAINT fk_teachers_user
        FOREIGN KEY (id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_teachers_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 6. REGISTRATION PERIODS
-- ============================================================

CREATE TABLE registration_periods (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    period_type VARCHAR(20) NOT NULL,
    teacher_start_date DATETIME NOT NULL,
    teacher_end_date DATETIME NOT NULL,
    student_start_date DATETIME NOT NULL,
    student_end_date DATETIME NOT NULL,
    review_submission_deadline DATETIME DEFAULT NULL,
    council_report_date DATETIME DEFAULT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 7. STUDENT GROUPS
-- ============================================================

CREATE TABLE student_groups (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_code VARCHAR(30) NOT NULL,
    period_id BIGINT NOT NULL,
    leader_student_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'FORMING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_student_groups_period
        FOREIGN KEY (period_id)
        REFERENCES registration_periods(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_student_groups_leader
        FOREIGN KEY (leader_student_id)
        REFERENCES students(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 8. GROUP MEMBERS
-- ============================================================

CREATE TABLE group_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_group_member (group_id, student_id),

    CONSTRAINT fk_group_members_group
        FOREIGN KEY (group_id)
        REFERENCES student_groups(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_group_members_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 9. TOPICS
-- ============================================================

CREATE TABLE topics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT DEFAULT NULL,
    department_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    created_by_teacher_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_topics_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_topics_period
        FOREIGN KEY (period_id)
        REFERENCES registration_periods(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_topics_teacher
        FOREIGN KEY (created_by_teacher_id)
        REFERENCES teachers(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 10. TOPIC ADVISORS
-- ============================================================

CREATE TABLE topic_advisors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    topic_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    advisor_role VARCHAR(20) NOT NULL DEFAULT 'PRIMARY',

    PRIMARY KEY (id),
    UNIQUE KEY uq_topic_advisor (topic_id, teacher_id),

    CONSTRAINT fk_topic_advisors_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_topic_advisors_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES teachers(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 11. TOPIC REGISTRATIONS
-- ============================================================

CREATE TABLE topic_registrations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT DEFAULT NULL,
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_topic_registration_group (group_id),
    UNIQUE KEY uk_topic_registration_topic (topic_id),

    CONSTRAINT fk_topic_registrations_group
        FOREIGN KEY (group_id)
        REFERENCES student_groups(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_topic_registrations_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 12. REVIEW COUNCILS
-- ============================================================

CREATE TABLE review_councils (
    id BIGINT NOT NULL AUTO_INCREMENT,
    council_code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    period_id BIGINT NOT NULL,
    defense_date DATETIME DEFAULT NULL,
    location VARCHAR(150) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_review_councils_code (council_code),

    CONSTRAINT fk_review_councils_period
        FOREIGN KEY (period_id)
        REFERENCES registration_periods(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 13. COUNCIL MEMBERS
-- ============================================================

CREATE TABLE council_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    council_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',

    PRIMARY KEY (id),
    UNIQUE KEY uq_council_member (council_id, teacher_id),

    CONSTRAINT fk_council_members_council
        FOREIGN KEY (council_id)
        REFERENCES review_councils(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_council_members_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES teachers(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 14. COUNCIL TOPICS
-- ============================================================

CREATE TABLE council_topics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    council_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_council_topic (topic_id),

    CONSTRAINT fk_council_topics_council
        FOREIGN KEY (council_id)
        REFERENCES review_councils(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_council_topics_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 15. SUBMISSIONS
-- ============================================================

CREATE TABLE submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    version_number INT NOT NULL DEFAULT 1,
    uploaded_by_student_id BIGINT NOT NULL,
    notes TEXT DEFAULT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_submissions_group
        FOREIGN KEY (group_id)
        REFERENCES student_groups(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_submissions_student
        FOREIGN KEY (uploaded_by_student_id)
        REFERENCES students(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 16. SCORES
-- ============================================================

CREATE TABLE scores (
    id BIGINT NOT NULL AUTO_INCREMENT,
    topic_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    score_value DECIMAL(4,2) NOT NULL,
    comments TEXT DEFAULT NULL,
    scored_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_score (topic_id, teacher_id),

    CONSTRAINT fk_scores_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_scores_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES teachers(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 17. FINAL RESULTS
-- ============================================================

CREATE TABLE final_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    topic_id BIGINT NOT NULL,
    final_score DECIMAL(4,2) DEFAULT NULL,
    evaluation_status VARCHAR(20) DEFAULT NULL,
    chairman_comments TEXT DEFAULT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at DATETIME DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_final_result_topic (topic_id),

    CONSTRAINT fk_final_results_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 18. NOTIFICATIONS
-- ============================================================

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    target_role VARCHAR(30) DEFAULT NULL,
    period_id BIGINT DEFAULT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_notifications_period
        FOREIGN KEY (period_id)
        REFERENCES registration_periods(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (created_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- HOÀN TẤT
-- ============================================================

SELECT 'Database structure created successfully!' AS message;