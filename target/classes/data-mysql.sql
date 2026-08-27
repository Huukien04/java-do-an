-- ============================================================
-- SCRIPT CHÈN DỮ LIỆU MẪU (SEED DATA) CHO MYSQL / MARIADB
-- HỆ THỐNG QUẢN LÝ ĐỀ TÀI (TOPIC MANAGEMENT SYSTEM)
-- Mật khẩu mặc định: 
--   Admin (admin): admin123
--   Giảng viên (gv_*): 123456
--   Sinh viên (sv_*): 123456
-- ============================================================

-- 1. Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_HEAD_OF_DEPARTMENT'),
(3, 'ROLE_TEACHER'),
(4, 'ROLE_STUDENT')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Departments
INSERT INTO departments (id, code, name, description, created_at) VALUES 
(1, 'KTPM', 'Kỹ thuật Phần mềm', 'Bộ môn Kỹ thuật Phần mềm', NOW()),
(2, 'HTTT', 'Hệ thống Thông tin', 'Bộ môn Hệ thống Thông tin', NOW()),
(3, 'KHMT', 'Khoa học Máy tính', 'Bộ môn Khoa học Máy tính', NOW()),
(4, 'MMT', 'Mạng máy tính & Truyền thông', 'Bộ môn Mạng máy tính', NOW()),
(5, 'CNTT', 'Công nghệ Thông tin', 'Bộ môn Công nghệ Thông tin', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Users (Admin, Teachers, Students)
-- Pass admin: admin123 ($2a$10$GRLdNijSQMUvl/au9ofL.e34LfZueVzhBgeX0GsPId3r5FB3B2Fj6)
-- Pass users: 123456 ($2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a)
INSERT INTO users (id, username, password, full_name, email, phone, enabled, role_id, created_at) VALUES
(1, 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.e34LfZueVzhBgeX0GsPId3r5FB3B2Fj6', 'GS.TS. Trưởng Khoa CNTT', 'truongkhoa@hcmute.edu.vn', '0901234567', TRUE, 1, NOW()),

-- Teachers (ID 2 -> 6)
(2, 'gv_nguyenvana', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'TS. Nguyễn Văn A', 'gv.nguyenvana@hcmute.edu.vn', '0912345678', TRUE, 3, NOW()),
(3, 'gv_tranvanb', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ThS. Trần Văn B', 'gv.tranvanb@hcmute.edu.vn', '0912345678', TRUE, 3, NOW()),
(4, 'gv_lethic', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'PGS.TS. Lê Thị C', 'gv.lethic@hcmute.edu.vn', '0912345678', TRUE, 3, NOW()),
(5, 'gv_phamvand', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'TS. Phạm Văn D', 'gv.phamvand@hcmute.edu.vn', '0912345678', TRUE, 3, NOW()),
(6, 'gv_hoangvane', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ThS. Hoàng Văn E', 'gv.hoangvane@hcmute.edu.vn', '0912345678', TRUE, 3, NOW()),

-- Students (ID 7 -> 11)
(7, 'sv_nguyenvanf', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Nguyễn Văn F', 'sv.nguyenvanf@student.hcmute.edu.vn', '0987654321', TRUE, 4, NOW()),
(8, 'sv_tranvang', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Trần Văn G', 'sv.tranvang@student.hcmute.edu.vn', '0987654321', TRUE, 4, NOW()),
(9, 'sv_lethih', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Lê Thị H', 'sv.lethih@student.hcmute.edu.vn', '0987654321', TRUE, 4, NOW()),
(10, 'sv_phamvani', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Phạm Văn I', 'sv.phamvani@student.hcmute.edu.vn', '0987654321', TRUE, 4, NOW()),
(11, 'sv_hoangvank', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Hoàng Văn K', 'sv.hoangvank@student.hcmute.edu.vn', '0987654321', TRUE, 4, NOW())
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 4. Teachers table
INSERT INTO teachers (id, teacher_code, academic_rank, degree, department_id) VALUES
(2, 'GV001', 'TS', 'Tiến sĩ', 1),
(3, 'GV002', 'ThS', 'Thạc sĩ', 2),
(4, 'GV003', 'PGS.TS', 'Phó Giáo sư Tiến sĩ', 3),
(5, 'GV004', 'TS', 'Tiến sĩ', 4),
(6, 'GV005', 'ThS', 'Thạc sĩ', 5)
ON DUPLICATE KEY UPDATE teacher_code=VALUES(teacher_code);

-- 5. Students table
INSERT INTO students (id, student_code, class_name, academic_year, department_id) VALUES
(7, 'SV2001', 'ĐH KTPM16A', '2022-2026', 1),
(8, 'SV2002', 'ĐH KTPM16A', '2022-2026', 1),
(9, 'SV2003', 'ĐH KTPM16A', '2022-2026', 1),
(10, 'SV2004', 'ĐH HTTT16A', '2022-2026', 2),
(11, 'SV2005', 'ĐH HTTT16A', '2022-2026', 2)
ON DUPLICATE KEY UPDATE student_code=VALUES(student_code);

-- 6. Sample Registration Period
INSERT INTO registration_periods 
(id, name, period_type, teacher_start_date, teacher_end_date, student_start_date, student_end_date, review_submission_deadline, council_report_date, status, created_at)
VALUES 
(1, 'Đợt Khóa luận Tốt nghiệp Học kỳ 1 (2024-2025)', 'KHAN_LUAN', '2024-09-01 00:00:00', '2024-09-15 23:59:59', '2024-09-16 00:00:00', '2024-09-30 23:59:59', '2024-11-30 23:59:59', '2024-12-15 08:00:00', 'STUDENT_REGISTRATION', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 7. Sample Topics
INSERT INTO topics (id, title, description, requirements, department_id, period_id, status, created_by_teacher_id, created_at) VALUES
(1, 'Xây dựng hệ thống Quản lý Đề tài Niên luận Khóa luận', 'Nghiên cứu thiết kế và phát triển hệ thống hỗ trợ đăng ký, duyệt đề tài và phân công hội đồng chấm điểm.', 'Hiểu biết về Spring Boot, Thymeleaf, MySQL và JPA', 1, 1, 'APPROVED', 2, NOW()),
(2, 'Ứng dụng AI trong nhận diện khuôn mặt sinh viên điểm danh', 'Phát triển mô hình Deep Learning hỗ trợ tự động nhận diện sinh viên trong phòng máy.', 'Thành thạo Python, OpenCV, PyTorch hoặc TensorFlow', 3, 1, 'APPROVED', 4, NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 8. Topic Advisors
INSERT INTO topic_advisors (id, topic_id, teacher_id, advisor_role) VALUES
(1, 1, 2, 'PRIMARY'),
(2, 2, 4, 'PRIMARY')
ON DUPLICATE KEY UPDATE advisor_role=VALUES(advisor_role);
