package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.TopicDTO;

import java.util.List;

public interface TopicService {
    TopicDTO createTopic(TopicDTO dto, Long currentTeacherId);
    TopicDTO updateTopic(Long id, TopicDTO dto, Long currentTeacherId);
    void deleteTopic(Long id, Long currentTeacherId);
    void approveTopic(Long topicId);
    void rejectTopic(Long topicId, String reason);
    TopicDTO getTopicById(Long topicId);
    List<TopicDTO> getTopicsByPeriod(Long periodId);
    List<TopicDTO> getApprovedTopicsByPeriod(Long periodId);
    List<TopicDTO> getTopicsByTeacher(Long teacherId);
    List<TopicDTO> getAllTopics();
}
