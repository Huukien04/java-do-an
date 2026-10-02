package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.entity.Department;
import com.hcmute.topicmanagement.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/departments")
@RequiredArgsConstructor
public class AdminDepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "admin/departments/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        model.addAttribute("isEdit", false);
        return "admin/departments/form";
    }

    @PostMapping("/create")
    public String createDepartment(@ModelAttribute("department") Department department, RedirectAttributes redirectAttributes, Model model) {
        try {
            departmentService.create(department);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo Bộ môn mới thành công!");
            return "redirect:/admin/departments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/departments/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Department department = departmentService.findById(id);
        model.addAttribute("department", department);
        model.addAttribute("isEdit", true);
        return "admin/departments/form";
    }

    @PostMapping("/{id}/edit")
    public String updateDepartment(@PathVariable Long id, @ModelAttribute("department") Department department, RedirectAttributes redirectAttributes, Model model) {
        try {
            departmentService.update(id, department);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật Bộ môn thành công!");
            return "redirect:/admin/departments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", true);
            return "admin/departments/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa Bộ môn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa bộ môn này vì có giảng viên/sinh viên đang thuộc bộ môn. Error: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }
}
