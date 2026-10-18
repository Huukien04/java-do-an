package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.entity.Submission;
import com.hcmute.topicmanagement.enums.RoleName;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {
    Submission uploadSubmission(Long groupId, MultipartFile reportFile, MultipartFile sourceFile,
                                String demoUrl, String notes, Long currentStudentId);
    List<Submission> getSubmissionsByGroup(Long groupId, Long userId, RoleName role,
                                           Long studentId, Long teacherId);
    Submission getLatestSubmissionByGroup(Long groupId);
    Submission getSubmission(Long submissionId, Long userId, RoleName role,
                             Long studentId, Long teacherId);
}
