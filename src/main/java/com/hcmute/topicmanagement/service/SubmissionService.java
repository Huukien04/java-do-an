package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.entity.Submission;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {
    Submission uploadSubmission(Long groupId, MultipartFile file, String notes, Long currentStudentId);
    List<Submission> getSubmissionsByGroup(Long groupId);
    Submission getLatestSubmissionByGroup(Long groupId);
}
