package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.DepartmentForm;
import com.hcmute.topicmanagement.dto.UserAccountForm;
import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Transactional
public class AdminManagementController {

    private static final List<RoleName> MANAGED_ROLES =
            List.of(RoleName.ROLE_ADMIN, RoleName.ROLE_TEACHER, RoleName.ROLE_STUDENT);

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository topicAdvisorRepository;
    private final TopicReviewerRepository topicReviewerRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final ScoreRepository scoreRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SubmissionRepository submissionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/departments";
    }

    @GetMapping("/departments/new")
    public String newDepartment(Model model) {
        model.addAttribute("departmentForm", new DepartmentForm());
        model.addAttribute("formTitle", "Thêm bộ môn");
        model.addAttribute("formAction", "/admin/departments");
        return "admin/department_form";
    }

    @PostMapping("/departments")
    public String createDepartment(@ModelAttribute DepartmentForm form, Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            Department department = new Department();
            applyDepartmentForm(department, form);
            departmentRepository.save(department);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm bộ môn.");
            return "redirect:/admin/departments";
        } catch (BusinessException e) {
            model.addAttribute("departmentForm", form);
            model.addAttribute("formTitle", "Thêm bộ môn");
            model.addAttribute("formAction", "/admin/departments");
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/department_form";
        }
    }

    @GetMapping("/departments/{id}/edit")
    public String editDepartment(@PathVariable Long id, Model model) {
        Department department = findDepartment(id);
        DepartmentForm form = new DepartmentForm();
        form.setCode(department.getCode());
        form.setName(department.getName());
        form.setDescription(department.getDescription());
        model.addAttribute("departmentForm", form);
        model.addAttribute("formTitle", "Chỉnh sửa bộ môn");
        model.addAttribute("formAction", "/admin/departments/" + id);
        return "admin/department_form";
    }

    @PostMapping("/departments/{id}")
    public String updateDepartment(@PathVariable Long id, @ModelAttribute DepartmentForm form,
                                   Model model, RedirectAttributes redirectAttributes) {
        Department department = findDepartment(id);
        try {
            applyDepartmentForm(department, form);
            departmentRepository.save(department);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật bộ môn.");
            return "redirect:/admin/departments";
        } catch (BusinessException e) {
            model.addAttribute("departmentForm", form);
            model.addAttribute("formTitle", "Chỉnh sửa bộ môn");
            model.addAttribute("formAction", "/admin/departments/" + id);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/department_form";
        }
    }

    @PostMapping("/departments/{id}/delete")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Department department = findDepartment(id);
        if (teacherRepository.existsByDepartmentId(id)
                || studentRepository.existsByDepartmentId(id)
                || topicRepository.existsByDepartmentId(id)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa bộ môn đang được giảng viên, sinh viên hoặc đề tài sử dụng.");
            return "redirect:/admin/departments";
        }
        departmentRepository.delete(department);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bộ môn.");
        return "redirect:/admin/departments";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", userRepository.findAll());
        return "admin/accounts";
    }

    @GetMapping("/accounts/new")
    public String newAccount(Model model) {
        model.addAttribute("accountForm", new UserAccountForm());
        prepareAccountForm(model, "Tạo tài khoản", "/admin/accounts");
        return "admin/account_form";
    }

    @PostMapping("/accounts")
    public String createAccount(@ModelAttribute UserAccountForm form, Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            validateAccountForm(form, null, true);
            User user = User.builder()
                    .username(form.getUsername().trim())
                    .password(passwordEncoder.encode(form.getPassword()))
                    .fullName(form.getFullName().trim())
                    .email(form.getEmail().trim())
                    .phone(trimToNull(form.getPhone()))
                    .enabled(form.isEnabled())
                    .role(findRole(form.getRoleName()))
                    .build();
            user = userRepository.save(user);
            saveAccountProfile(user, form, null);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo tài khoản.");
            return "redirect:/admin/accounts";
        } catch (BusinessException e) {
            model.addAttribute("accountForm", form);
            prepareAccountForm(model, "Tạo tài khoản", "/admin/accounts");
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/account_form";
        }
    }

    @GetMapping("/accounts/{id}/edit")
    public String editAccount(@PathVariable Long id, Model model) {
        User user = findUser(id);
        UserAccountForm form = toAccountForm(user);
        model.addAttribute("accountForm", form);
        prepareAccountForm(model, "Chỉnh sửa tài khoản", "/admin/accounts/" + id);
        return "admin/account_form";
    }

    @PostMapping("/accounts/{id}")
    public String updateAccount(@PathVariable Long id, @ModelAttribute UserAccountForm form,
                                Model model, RedirectAttributes redirectAttributes) {
        User user = findUser(id);
        try {
            validateAccountForm(form, id, false);
            RoleName oldRole = user.getRole().getName();
            if (oldRole != form.getRoleName()) {
                ensureProfileCanBeRemoved(user);
                teacherRepository.findById(id).ifPresent(teacherRepository::delete);
                studentRepository.findById(id).ifPresent(studentRepository::delete);
            }

            user.setUsername(form.getUsername().trim());
            user.setFullName(form.getFullName().trim());
            user.setEmail(form.getEmail().trim());
            user.setPhone(trimToNull(form.getPhone()));
            user.setEnabled(form.isEnabled());
            user.setRole(findRole(form.getRoleName()));
            if (form.getPassword() != null && !form.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(form.getPassword()));
            }
            userRepository.save(user);
            saveAccountProfile(user, form, id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tài khoản.");
            return "redirect:/admin/accounts";
        } catch (BusinessException e) {
            form.setPassword(null);
            model.addAttribute("accountForm", form);
            prepareAccountForm(model, "Chỉnh sửa tài khoản", "/admin/accounts/" + id);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/account_form";
        }
    }

    @PostMapping("/accounts/{id}/delete")
    public String deleteAccount(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails currentUser,
                                RedirectAttributes redirectAttributes) {
        User user = findUser(id);
        if (currentUser != null && id.equals(currentUser.getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tài khoản đang đăng nhập.");
            return "redirect:/admin/accounts";
        }

        String usage = accountUsage(user);
        if (usage != null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa tài khoản vì đang được sử dụng: " + usage + ".");
            return "redirect:/admin/accounts";
        }

        teacherRepository.findById(id).ifPresent(teacherRepository::delete);
        studentRepository.findById(id).ifPresent(studentRepository::delete);
        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản.");
        return "redirect:/admin/accounts";
    }

    private void applyDepartmentForm(Department department, DepartmentForm form) {
        String code = trimToNull(form.getCode());
        String name = trimToNull(form.getName());
        if (code == null || name == null) {
            throw new BusinessException("Mã bộ môn và tên bộ môn không được để trống.");
        }
        boolean duplicateCode = department.getId() == null
                ? departmentRepository.existsByCode(code)
                : departmentRepository.findByCode(code)
                        .filter(existing -> !existing.getId().equals(department.getId())).isPresent();
        if (duplicateCode) {
            throw new BusinessException("Mã bộ môn đã tồn tại.");
        }
        department.setCode(code);
        department.setName(name);
        department.setDescription(trimToNull(form.getDescription()));
    }

    private void validateAccountForm(UserAccountForm form, Long userId, boolean creating) {
        if (trimToNull(form.getUsername()) == null || trimToNull(form.getFullName()) == null
                || trimToNull(form.getEmail()) == null || form.getRoleName() == null) {
            throw new BusinessException("Tên đăng nhập, họ tên, email và vai trò là bắt buộc.");
        }
        if (creating && (form.getPassword() == null || form.getPassword().isBlank())) {
            throw new BusinessException("Mật khẩu là bắt buộc khi tạo tài khoản.");
        }
        if (creating ? userRepository.existsByUsername(form.getUsername().trim())
                : userRepository.existsByUsernameAndIdNot(form.getUsername().trim(), userId)) {
            throw new BusinessException("Tên đăng nhập đã tồn tại.");
        }
        if (creating ? userRepository.existsByEmail(form.getEmail().trim())
                : userRepository.existsByEmailAndIdNot(form.getEmail().trim(), userId)) {
            throw new BusinessException("Email đã được sử dụng.");
        }
        if (!MANAGED_ROLES.contains(form.getRoleName())) {
            throw new BusinessException("Vai trò tài khoản không được hỗ trợ.");
        }

        if (form.getRoleName() == RoleName.ROLE_TEACHER) {
            if (trimToNull(form.getTeacherCode()) == null || form.getDepartmentId() == null) {
                throw new BusinessException("Tài khoản giảng viên cần mã giảng viên và bộ môn.");
            }
            boolean duplicate = userId == null
                    ? teacherRepository.existsByTeacherCode(form.getTeacherCode().trim())
                    : teacherRepository.existsByTeacherCodeAndIdNot(form.getTeacherCode().trim(), userId);
            if (duplicate) {
                throw new BusinessException("Mã giảng viên đã tồn tại.");
            }
        }
        if (form.getRoleName() == RoleName.ROLE_STUDENT) {
            if (trimToNull(form.getStudentCode()) == null || trimToNull(form.getClassName()) == null
                    || form.getDepartmentId() == null) {
                throw new BusinessException("Tài khoản sinh viên cần mã sinh viên, lớp và bộ môn.");
            }
            boolean duplicate = userId == null
                    ? studentRepository.existsByStudentCode(form.getStudentCode().trim())
                    : studentRepository.existsByStudentCodeAndIdNot(form.getStudentCode().trim(), userId);
            if (duplicate) {
                throw new BusinessException("Mã sinh viên đã tồn tại.");
            }
        }
        if (form.getDepartmentId() != null) {
            findDepartment(form.getDepartmentId());
        }
    }

    private void saveAccountProfile(User user, UserAccountForm form, Long id) {
        if (form.getRoleName() == RoleName.ROLE_TEACHER) {
            Teacher teacher = teacherRepository.findById(user.getId()).orElseGet(Teacher::new);
            teacher.setUser(user);
            teacher.setTeacherCode(form.getTeacherCode().trim());
            teacher.setAcademicRank(trimToNull(form.getAcademicRank()));
            teacher.setDegree(trimToNull(form.getDegree()));
            teacher.setDepartment(findDepartment(form.getDepartmentId()));
            teacherRepository.save(teacher);
        } else if (form.getRoleName() == RoleName.ROLE_STUDENT) {
            Student student = studentRepository.findById(user.getId()).orElseGet(Student::new);
            student.setUser(user);
            student.setStudentCode(form.getStudentCode().trim());
            student.setClassName(form.getClassName().trim());
            student.setAcademicYear(trimToNull(form.getAcademicYear()));
            student.setDepartment(findDepartment(form.getDepartmentId()));
            studentRepository.save(student);
        }
    }

    private void ensureProfileCanBeRemoved(User user) {
        String usage = teacherRepository.findById(user.getId()).map(this::teacherUsage).orElse(null);
        if (usage == null) {
            usage = studentRepository.findById(user.getId()).map(this::studentUsage).orElse(null);
        }
        if (usage != null) {
            throw new BusinessException("Không thể đổi vai trò vì hồ sơ đang được sử dụng: " + usage + ".");
        }
    }

    private String accountUsage(User user) {
        String teacherUsage = teacherRepository.findById(user.getId()).map(this::teacherUsage).orElse(null);
        if (teacherUsage != null) {
            return teacherUsage;
        }
        String studentUsage = studentRepository.findById(user.getId()).map(this::studentUsage).orElse(null);
        if (studentUsage != null) {
            return studentUsage;
        }
        if (notificationRepository.existsByCreatedById(user.getId())) {
            return "người tạo thông báo";
        }
        if (notificationRepository.existsByRecipients_Id(user.getId())) {
            return "người nhận thông báo";
        }
        if (notificationReadRepository.existsByUserId(user.getId())) {
            return "lịch sử đọc thông báo";
        }
        return null;
    }

    private String teacherUsage(Teacher teacher) {
        Long id = teacher.getId();
        if (topicRepository.existsByCreatedByTeacherId(id)) return "đề tài đã tạo";
        if (topicAdvisorRepository.existsByTeacherId(id)) return "vai trò giảng viên hướng dẫn";
        if (topicReviewerRepository.existsByTeacherId(id)) return "vai trò giảng viên phản biện";
        if (councilMemberRepository.existsByTeacherId(id)) return "thành viên hội đồng";
        if (scoreRepository.existsByTeacherId(id)) return "điểm đã chấm";
        return null;
    }

    private String studentUsage(Student student) {
        Long id = student.getId();
        if (studentGroupRepository.existsByLeaderId(id)) return "nhóm do sinh viên làm trưởng nhóm";
        if (groupMemberRepository.existsByStudentId(id)) return "thành viên nhóm";
        if (submissionRepository.existsByUploadedBy_Id(id)) return "báo cáo đã nộp";
        return null;
    }

    private UserAccountForm toAccountForm(User user) {
        UserAccountForm form = new UserAccountForm();
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRoleName(user.getRole().getName());
        form.setEnabled(user.isEnabled());
        teacherRepository.findById(user.getId()).ifPresent(teacher -> {
            form.setTeacherCode(teacher.getTeacherCode());
            form.setAcademicRank(teacher.getAcademicRank());
            form.setDegree(teacher.getDegree());
            form.setDepartmentId(teacher.getDepartment().getId());
        });
        studentRepository.findById(user.getId()).ifPresent(student -> {
            form.setStudentCode(student.getStudentCode());
            form.setClassName(student.getClassName());
            form.setAcademicYear(student.getAcademicYear());
            form.setDepartmentId(student.getDepartment().getId());
        });
        return form;
    }

    private void prepareAccountForm(Model model, String title, String action) {
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles", MANAGED_ROLES);
    }

    private Role findRole(RoleName name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new BusinessException("Không tìm thấy vai trò " + name + "."));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bộ môn."));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản."));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
