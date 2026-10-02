package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.UserFormDto;
import com.hcmute.topicmanagement.entity.User;
import com.hcmute.topicmanagement.repository.RoleRepository;
import com.hcmute.topicmanagement.service.DepartmentService;
import com.hcmute.topicmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final DepartmentService departmentService;

    @GetMapping
    public String listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long departmentId,
            Model model
    ) {
        List<User> users = userService.findAllUsers(keyword, roleId, departmentId);
        model.addAttribute("users", users);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRoleId", roleId);
        model.addAttribute("selectedDepartmentId", departmentId);
        return "admin/users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userForm", new UserFormDto());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/users/form";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute("userForm") UserFormDto userForm, RedirectAttributes redirectAttributes, Model model) {
        try {
            userService.createUser(userForm);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản mới thành công!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("isEdit", false);
            return "admin/users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        UserFormDto userForm = userService.getUserFormDtoById(id);
        model.addAttribute("userForm", userForm);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/users/form";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute("userForm") UserFormDto userForm, RedirectAttributes redirectAttributes, Model model) {
        try {
            userService.updateUser(id, userForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin tài khoản thành công!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/users/form";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam(required = false) String newPassword, RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công (Mặc định: 123456)!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tài khoản này (có thể liên quan đến dữ liệu đề tài/nhóm/điểm). Error: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
