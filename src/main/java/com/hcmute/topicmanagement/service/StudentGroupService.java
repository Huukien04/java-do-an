package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.StudentGroupDTO;

import java.util.List;

public interface StudentGroupService {
    StudentGroupDTO createGroup(Long leaderStudentId, Long periodId);
    StudentGroupDTO addMember(Long groupId, String studentCode, Long currentStudentId);
    void removeMember(Long groupId, Long memberStudentId, Long currentStudentId);
    StudentGroupDTO registerTopic(Long groupId, Long topicId, Long currentStudentId);
    void disbandRejectedGroup(Long groupId, Long currentStudentId);
    void approveTopicRegistration(Long registrationId);
    void rejectTopicRegistration(Long registrationId, String reason);
    StudentGroupDTO getGroupById(Long groupId);
    StudentGroupDTO getMyGroupByPeriod(Long studentId, Long periodId);
    List<StudentGroupDTO> getGroupsByPeriod(Long periodId);
}
