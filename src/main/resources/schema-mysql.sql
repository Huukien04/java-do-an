-- ============================================================
-- 02_seed_data.sql
-- MYSQL / MARIADB
-- TOPIC MANAGEMENT SYSTEM
--
-- Tài khoản:
--   admin / admin123
--   gv_nguyenvana / 123456
--   gv_tranvanb / 123456
--   gv_lethic / 123456
--   gv_phamvand / 123456
--   gv_hoangvane / 123456
--   sv_nguyenvanf / 123456
--   sv_tranvang / 123456
--   sv_lethih / 123456
--   sv_phamvani / 123456
--   sv_hoangvank / 123456
-- ============================================================


START TRANSACTION;


-- ============================================================
-- 1. ROLES
-- ============================================================

INSERT INTO roles (id, name)
VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_HEAD_OF_DEPARTMENT'),
    (3, 'ROLE_TEACHER'),
    (4, 'ROLE_STUDENT')
ON DUPLICATE KEY UPDATE
    name = VALUES(name);


-- ============================================================
-- 2. DEPARTMENTS
-- ============================================================

INSERT INTO departments
    (id, code, name, description, created_at)
VALUES
    (1, 'KTPM',
     'Kỹ thuật Phần mềm',
     'Bộ môn Kỹ thuật Phần mềm',
     NOW()),

    (2, 'HTTT',
     'Hệ thống Thông tin',
     'Bộ môn Hệ thống Thông tin',
     NOW()),

    (3, 'KHMT',
     'Khoa học Máy tính',
     'Bộ môn Khoa học Máy tính',
     NOW()),

    (4, 'MMT',
     'Mạng máy tính & Truyền thông',
     'Bộ môn Mạng máy tính',
     NOW()),

    (5, 'CNTT',
     'Công nghệ Thông tin',
     'Bộ môn Công nghệ Thông tin',
     NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description);


-- ============================================================
-- 3. USERS
-- ============================================================
--
-- BCrypt:
-- admin123
-- $2a$10$GRLdNijSQMUvl/au9ofL.e34LfZueVzhBgeX0GsPId3r5FB3B2Fj6
--
-- 123456
-- $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a
-- ============================================================

INSERT INTO users
    (id, username, password, full_name, email, phone, enabled, role_id, created_at)
VALUES

    (1,
     'admin',
     '$2a$10$GRLdNijSQMUvl/au9ofL.e34LfZueVzhBgeX0GsPId3r5FB3B2Fj6',
     'GS.TS. Trưởng Khoa CNTT',
     'truongkhoa@hcmute.edu.vn',
     '0901234567',
     TRUE,
     1,
     NOW()),

    (2,
     'gv_nguyenvana',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'TS. Nguyễn Văn A',
     'gv.nguyenvana@hcmute.edu.vn',
     '0912345678',
     TRUE,
     3,
     NOW()),

    (3,
     'gv_tranvanb',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'ThS. Trần Văn B',
     'gv.tranvanb@hcmute.edu.vn',
     '0912345679',
     TRUE,
     3,
     NOW()),

    (4,
     'gv_lethic',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'PGS.TS. Lê Thị C',
     'gv.lethic@hcmute.edu.vn',
     '0912345680',
     TRUE,
     3,
     NOW()),

    (5,
     'gv_phamvand',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'TS. Phạm Văn D',
     'gv.phamvand@hcmute.edu.vn',
     '0912345681',
     TRUE,
     3,
     NOW()),

    (6,
     'gv_hoangvane',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'ThS. Hoàng Văn E',
     'gv.hoangvane@hcmute.edu.vn',
     '0912345682',
     TRUE,
     3,
     NOW()),

    (7,
     'sv_nguyenvanf',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'Nguyễn Văn F',
     'sv.nguyenvanf@student.hcmute.edu.vn',
     '0987654321',
     TRUE,
     4,
     NOW()),

    (8,
     'sv_tranvang',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'Trần Văn G',
     'sv.tranvang@student.hcmute.edu.vn',
     '0987654322',
     TRUE,
     4,
     NOW()),

    (9,
     'sv_lethih',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'Lê Thị H',
     'sv.lethih@student.hcmute.edu.vn',
     '0987654323',
     TRUE,
     4,
     NOW()),

    (10,
     'sv_phamvani',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'Phạm Văn I',
     'sv.phamvani@student.hcmute.edu.vn',
     '0987654324',
     TRUE,
     4,
     NOW()),

    (11,
     'sv_hoangvank',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
     'Hoàng Văn K',
     'sv.hoangvank@student.hcmute.edu.vn',
     '0987654325',
     TRUE,
     4,
     NOW())

ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    full_name = VALUES(full_name),
    email = VALUES(email),
    phone = VALUES(phone),
    enabled = VALUES(enabled),
    role_id = VALUES(role_id);


-- ============================================================
-- 4. TEACHERS
-- ============================================================

INSERT INTO teachers
    (id, teacher_code, academic_rank, degree, department_id)
VALUES
    (2, 'GV001', 'TS',     'Tiến sĩ',                 1),
    (3, 'GV002', 'ThS',    'Thạc sĩ',                 2),
    (4, 'GV003', 'PGS.TS', 'Phó Giáo sư Tiến sĩ',     3),
    (5, 'GV004', 'TS',     'Tiến sĩ',                 4),
    (6, 'GV005', 'ThS',    'Thạc sĩ',                 5)

ON DUPLICATE KEY UPDATE
    teacher_code = VALUES(teacher_code),
    academic_rank = VALUES(academic_rank),
    degree = VALUES(degree),
    department_id = VALUES(department_id);


-- ============================================================
-- 5. STUDENTS
-- ============================================================

INSERT INTO students
    (id, student_code, class_name, academic_year, department_id)
VALUES
    (7,  'SV2001', 'ĐH KTPM16A', '2022-2026', 1),
    (8,  'SV2002', 'ĐH KTPM16A', '2022-2026', 1),
    (9,  'SV2003', 'ĐH KTPM16A', '2022-2026', 1),
    (10, 'SV2004', 'ĐH HTTT16A', '2022-2026', 2),
    (11, 'SV2005', 'ĐH HTTT16A', '2022-2026', 2)

ON DUPLICATE KEY UPDATE
    student_code = VALUES(student_code),
    class_name = VALUES(class_name),
    academic_year = VALUES(academic_year),
    department_id = VALUES(department_id);


-- ============================================================
-- 6. REGISTRATION PERIOD
-- ============================================================

INSERT INTO registration_periods
(
    id,
    name,
    period_type,
    teacher_start_date,
    teacher_end_date,
    student_start_date,
    student_end_date,
    review_submission_deadline,
    council_report_date,
    status,
    created_at
)
VALUES
(
    1,
    'Đợt Khóa luận Tốt nghiệp Học kỳ 1 (2026-2027)',
    'KHAN_LUAN',
    '2026-09-01 00:00:00',
    '2026-09-15 23:59:59',
    '2026-09-16 00:00:00',
    '2026-09-30 23:59:59',
    '2026-11-30 23:59:59',
    '2026-12-15 08:00:00',
    'STUDENT_REGISTRATION',
    NOW()
)

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    period_type = VALUES(period_type),
    teacher_start_date = VALUES(teacher_start_date),
    teacher_end_date = VALUES(teacher_end_date),
    student_start_date = VALUES(student_start_date),
    student_end_date = VALUES(student_end_date),
    review_submission_deadline = VALUES(review_submission_deadline),
    council_report_date = VALUES(council_report_date),
    status = VALUES(status);


-- ============================================================
-- 7. TOPICS
-- ============================================================

INSERT INTO topics
(
    id,
    title,
    description,
    requirements,
    department_id,
    period_id,
    status,
    created_by_teacher_id,
    created_at
)
VALUES

(
    1,
    'Xây dựng hệ thống Quản lý Đề tài Niên luận Khóa luận',
    'Nghiên cứu thiết kế và phát triển hệ thống hỗ trợ đăng ký, duyệt đề tài và phân công hội đồng chấm điểm.',
    'Hiểu biết về Spring Boot, Thymeleaf, MySQL và JPA',
    1,
    1,
    'APPROVED',
    2,
    NOW()
),

(
    2,
    'Ứng dụng AI trong nhận diện khuôn mặt sinh viên điểm danh',
    'Phát triển mô hình Deep Learning hỗ trợ tự động nhận diện sinh viên trong phòng máy.',
    'Thành thạo Python, OpenCV, PyTorch hoặc TensorFlow',
    3,
    1,
    'APPROVED',
    4,
    NOW()
)

ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    requirements = VALUES(requirements),
    department_id = VALUES(department_id),
    period_id = VALUES(period_id),
    status = VALUES(status),
    created_by_teacher_id = VALUES(created_by_teacher_id);


-- ============================================================
-- 8. TOPIC ADVISORS
-- ============================================================

INSERT INTO topic_advisors
    (id, topic_id, teacher_id, advisor_role)
VALUES
    (1, 1, 2, 'PRIMARY'),
    (2, 2, 4, 'PRIMARY')

ON DUPLICATE KEY UPDATE
    advisor_role = VALUES(advisor_role);


-- ============================================================
-- 9. STUDENT GROUPS
-- ============================================================

INSERT INTO student_groups
    (id, group_code, period_id, leader_student_id, status, created_at)
VALUES
    (1, 'N01', 1, 7, 'ACTIVE', NOW()),
    (2, 'N02', 1, 10, 'ACTIVE', NOW())

ON DUPLICATE KEY UPDATE
    group_code = VALUES(group_code),
    period_id = VALUES(period_id),
    leader_student_id = VALUES(leader_student_id),
    status = VALUES(status);


-- ============================================================
-- 10. GROUP MEMBERS
-- ============================================================

INSERT INTO group_members
    (id, group_id, student_id, joined_at)
VALUES
    (1, 1, 7,  NOW()),
    (2, 1, 8,  NOW()),
    (3, 1, 9,  NOW()),
    (4, 2, 10, NOW()),
    (5, 2, 11, NOW())

ON DUPLICATE KEY UPDATE
    joined_at = VALUES(joined_at);


-- ============================================================
-- 11. TOPIC REGISTRATIONS
-- ============================================================

INSERT INTO topic_registrations
(
    id,
    group_id,
    topic_id,
    status,
    rejection_reason,
    registered_at
)
VALUES
(
    1,
    1,
    1,
    'APPROVED',
    NULL,
    NOW()
),
(
    2,
    2,
    2,
    'APPROVED',
    NULL,
    NOW()
)

ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    rejection_reason = VALUES(rejection_reason);


-- ============================================================
-- 12. REVIEW COUNCILS
-- ============================================================

INSERT INTO review_councils
(
    id,
    council_code,
    name,
    period_id,
    defense_date,
    location,
    created_at
)
VALUES
(
    1,
    'HD01',
    'Hội đồng bảo vệ Khóa luận 01',
    1,
    '2026-12-15 08:00:00',
    'Phòng A101',
    NOW()
)

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    period_id = VALUES(period_id),
    defense_date = VALUES(defense_date),
    location = VALUES(location);


-- ============================================================
-- 13. COUNCIL MEMBERS
-- ============================================================

INSERT INTO council_members
    (id, council_id, teacher_id, role)
VALUES
    (1, 1, 2, 'CHAIRMAN'),
    (2, 1, 3, 'MEMBER'),
    (3, 1, 4, 'SECRETARY')

ON DUPLICATE KEY UPDATE
    role = VALUES(role);


-- ============================================================
-- 14. COUNCIL TOPICS
-- ============================================================

INSERT INTO council_topics
    (id, council_id, topic_id)
VALUES
    (1, 1, 1),
    (2, 1, 2)

ON DUPLICATE KEY UPDATE
    council_id = VALUES(council_id);


-- ============================================================
-- 15. SUBMISSIONS
-- ============================================================

INSERT INTO submissions
(
    id,
    group_id,
    file_name,
    file_path,
    file_size,
    version_number,
    uploaded_by_student_id,
    notes,
    uploaded_at
)
VALUES
(
    1,
    1,
    'khoa_luan_nhom_N01_v1.pdf',
    '/uploads/submissions/khoa_luan_nhom_N01_v1.pdf',
    5242880,
    1,
    7,
    'Bản nộp thử nghiệm',
    NOW()
)

ON DUPLICATE KEY UPDATE
    file_name = VALUES(file_name),
    file_path = VALUES(file_path),
    file_size = VALUES(file_size),
    version_number = VALUES(version_number),
    uploaded_by_student_id = VALUES(uploaded_by_student_id),
    notes = VALUES(notes);


-- ============================================================
-- 16. SCORES
-- ============================================================

INSERT INTO scores
(
    id,
    topic_id,
    teacher_id,
    score_value,
    comments,
    scored_at
)
VALUES
(
    1,
    1,
    2,
    8.50,
    'Đề tài có tính thực tiễn tốt.',
    NOW()
),
(
    2,
    1,
    3,
    8.00,
    'Giải pháp kỹ thuật phù hợp.',
    NOW()
),
(
    3,
    2,
    4,
    9.00,
    'Ứng dụng AI có tính thực tiễn cao.',
    NOW()
)

ON DUPLICATE KEY UPDATE
    score_value = VALUES(score_value),
    comments = VALUES(comments);


-- ============================================================
-- 17. FINAL RESULTS
-- ============================================================

INSERT INTO final_results
(
    id,
    topic_id,
    final_score,
    evaluation_status,
    chairman_comments,
    is_published,
    published_at
)
VALUES
(
    1,
    1,
    8.25,
    'PASSED',
    'Kết quả tốt, đáp ứng yêu cầu của đề tài.',
    TRUE,
    NOW()
),
(
    2,
    2,
    9.00,
    'PASSED',
    'Đề tài có chất lượng tốt.',
    TRUE,
    NOW()
)

ON DUPLICATE KEY UPDATE
    final_score = VALUES(final_score),
    evaluation_status = VALUES(evaluation_status),
    chairman_comments = VALUES(chairman_comments),
    is_published = VALUES(is_published),
    published_at = VALUES(published_at);


-- ============================================================
-- 18. NOTIFICATIONS
-- ============================================================

INSERT INTO notifications
(
    id,
    title,
    content,
    type,
    target_role,
    period_id,
    created_by_user_id,
    created_at
)
VALUES
(
    1,
    'Thông báo đăng ký đề tài',
    'Sinh viên vui lòng đăng ký đề tài theo thời gian quy định.',
    'REGISTRATION',
    'ROLE_STUDENT',
    1,
    1,
    NOW()
),
(
    2,
    'Thông báo duyệt đề tài',
    'Giảng viên vui lòng kiểm tra và duyệt các đề tài đã đăng ký.',
    'APPROVAL',
    'ROLE_TEACHER',
    1,
    1,
    NOW()
)

ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    type = VALUES(type),
    target_role = VALUES(target_role),
    period_id = VALUES(period_id);


-- ============================================================
-- COMMIT
-- ============================================================

COMMIT;


-- ============================================================
-- KIỂM TRA DỮ LIỆU
-- ============================================================

SELECT 'roles' AS table_name, COUNT(*) AS total FROM roles
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'departments', COUNT(*) FROM departments
UNION ALL
SELECT 'teachers', COUNT(*) FROM teachers
UNION ALL
SELECT 'students', COUNT(*) FROM students
UNION ALL
SELECT 'registration_periods', COUNT(*) FROM registration_periods
UNION ALL
SELECT 'student_groups', COUNT(*) FROM student_groups
UNION ALL
SELECT 'group_members', COUNT(*) FROM group_members
UNION ALL
SELECT 'topics', COUNT(*) FROM topics
UNION ALL
SELECT 'topic_advisors', COUNT(*) FROM topic_advisors
UNION ALL
SELECT 'topic_registrations', COUNT(*) FROM topic_registrations
UNION ALL
SELECT 'review_councils', COUNT(*) FROM review_councils
UNION ALL
SELECT 'council_members', COUNT(*) FROM council_members
UNION ALL
SELECT 'council_topics', COUNT(*) FROM council_topics
UNION ALL
SELECT 'submissions', COUNT(*) FROM submissions
UNION ALL
SELECT 'scores', COUNT(*) FROM scores
UNION ALL
SELECT 'final_results', COUNT(*) FROM final_results
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications;
