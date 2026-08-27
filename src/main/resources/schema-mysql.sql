-- ============================================================
-- SCRIPT TẠO CẤU TRÚC CƠ SỞ DỮ LIỆU MYSQL / MARIADB
-- HỆ THỐNG QUẢN LÝ ĐỀ TÀI (TOPIC MANAGEMENT SYSTEM)
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

-- 1. Roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) DEFAULT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Departments
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Students
CREATE TABLE students (
    id BIGINT PRIMARY KEY,
    student_code VARCHAR(20) NOT NULL UNIQUE,
    class_name VARCHAR(50) NOT NULL,
    academic_year VARCHAR(20) DEFAULT NULL,
    department_id BIGINT NOT NULL,
    CONSTRAINT fk_students_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_students_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Teachers
CREATE TABLE teachers (
    id BIGINT PRIMARY KEY,
    teacher_code VARCHAR(20) NOT NULL UNIQUE,
    academic_rank VARCHAR(50) DEFAULT NULL,
    degree VARCHAR(50) DEFAULT NULL,
    department_id BIGINT NOT NULL,
    CONSTRAINT fk_teachers_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_teachers_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Registration Periods
CREATE TABLE registration_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    period_type VARCHAR(20) NOT NULL,
    teacher_start_date DATETIME NOT NULL,
    teacher_end_date DATETIME NOT NULL,
    student_start_date DATETIME NOT NULL,
    student_end_date DATETIME NOT NULL,
    review_submission_deadline DATETIME DEFAULT NULL,
    council_report_date DATETIME DEFAULT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Student Groups
CREATE TABLE student_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_code VARCHAR(30) NOT NULL,
    period_id BIGINT NOT NULL,
    leader_student_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'FORMING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_groups_period FOREIGN KEY (period_id) REFERENCES registration_periods(id) ON DELETE RESTRICT,
    CONSTRAINT fk_student_groups_leader FOREIGN KEY (leader_student_id) REFERENCES students(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Group Members
CREATE TABLE group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_group_member UNIQUE (group_id, student_id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_members_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Topics
CREATE TABLE topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT DEFAULT NULL,
    department_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    created_by_teacher_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topics_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_topics_period FOREIGN KEY (period_id) REFERENCES registration_periods(id) ON DELETE RESTRICT,
    CONSTRAINT fk_topics_teacher FOREIGN KEY (created_by_teacher_id) REFERENCES teachers(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Topic Advisors
CREATE TABLE topic_advisors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    advisor_role VARCHAR(20) NOT NULL DEFAULT 'PRIMARY',
    CONSTRAINT uq_topic_advisor UNIQUE (topic_id, teacher_id),
    CONSTRAINT fk_topic_advisors_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    CONSTRAINT fk_topic_advisors_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Topic Registrations
CREATE TABLE topic_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL UNIQUE,
    topic_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT DEFAULT NULL,
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topic_registrations_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_topic_registrations_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Review Councils
CREATE TABLE review_councils (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    council_code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    period_id BIGINT NOT NULL,
    defense_date DATETIME DEFAULT NULL,
    location VARCHAR(150) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_councils_period FOREIGN KEY (period_id) REFERENCES registration_periods(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Council Members
CREATE TABLE council_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    council_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    CONSTRAINT uq_council_member UNIQUE (council_id, teacher_id),
    CONSTRAINT fk_council_members_council FOREIGN KEY (council_id) REFERENCES review_councils(id) ON DELETE CASCADE,
    CONSTRAINT fk_council_members_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Council Topics
CREATE TABLE council_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    council_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_council_topics_council FOREIGN KEY (council_id) REFERENCES review_councils(id) ON DELETE CASCADE,
    CONSTRAINT fk_council_topics_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Submissions
CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    version_number INT NOT NULL DEFAULT 1,
    uploaded_by_student_id BIGINT NOT NULL,
    notes TEXT DEFAULT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_submissions_group FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_student FOREIGN KEY (uploaded_by_student_id) REFERENCES students(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Scores
CREATE TABLE scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    score_value DECIMAL(4,2) NOT NULL,
    comments TEXT DEFAULT NULL,
    scored_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_score UNIQUE (topic_id, teacher_id),
    CONSTRAINT fk_scores_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    CONSTRAINT fk_scores_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Final Results
CREATE TABLE final_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL UNIQUE,
    final_score DECIMAL(4,2) DEFAULT NULL,
    evaluation_status VARCHAR(20) DEFAULT NULL,
    chairman_comments TEXT DEFAULT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_final_results_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    target_role VARCHAR(30) DEFAULT NULL,
    period_id BIGINT DEFAULT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_period FOREIGN KEY (period_id) REFERENCES registration_periods(id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
