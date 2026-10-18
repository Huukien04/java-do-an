package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.entity.Submission;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Controller
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public String uploadForm(@RequestParam Long groupId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        model.addAttribute("groupId", groupId);
        model.addAttribute("submissions", submissionsFor(groupId, userDetails));
        return "submission/upload";
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public String uploadSubmission(@RequestParam Long groupId,
                                   @RequestParam("file") MultipartFile reportFile,
                                   @RequestParam(value = "sourceFile", required = false) MultipartFile sourceFile,
                                   @RequestParam(required = false) String demoUrl,
                                   @RequestParam(required = false) String notes,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            submissionService.uploadSubmission(groupId, reportFile, sourceFile, demoUrl, notes,
                    userDetails.getStudent().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Nộp kết quả thành công; hệ thống đã lưu phiên bản mới.");
        } catch (org.springframework.security.access.AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/group";
    }

    @GetMapping("/versions/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public String versions(@PathVariable Long groupId,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        model.addAttribute("groupId", groupId);
        model.addAttribute("submissions", submissionsFor(groupId, userDetails));
        return "submission/versions";
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "false") boolean source,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Submission submission = submissionService.getSubmission(id, userDetails.getUserId(),
                userDetails.getUser().getRole().getName(),
                userDetails.getStudent() == null ? null : userDetails.getStudent().getId(),
                userDetails.getTeacher() == null ? null : userDetails.getTeacher().getId());
        String storedPath = source ? submission.getSourceFilePath() : submission.getFilePath();
        String downloadName = source ? submission.getSourceFileName() : submission.getFileName();
        if (storedPath == null || downloadName == null) {
            return ResponseEntity.notFound().build();
        }
        Path path = Path.of(storedPath).toAbsolutePath().normalize();
        File file = path.toFile();
        if (!file.isFile()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(downloadName, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private java.util.List<Submission> submissionsFor(Long groupId, CustomUserDetails userDetails) {
        return submissionService.getSubmissionsByGroup(groupId, userDetails.getUserId(),
                userDetails.getUser().getRole().getName(),
                userDetails.getStudent() == null ? null : userDetails.getStudent().getId(),
                userDetails.getTeacher() == null ? null : userDetails.getTeacher().getId());
    }
}
