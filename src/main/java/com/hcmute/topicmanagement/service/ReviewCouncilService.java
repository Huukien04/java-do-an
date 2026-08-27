package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.ReviewCouncilDTO;

import java.util.List;

public interface ReviewCouncilService {
    ReviewCouncilDTO createCouncil(ReviewCouncilDTO dto);
    ReviewCouncilDTO updateCouncil(Long id, ReviewCouncilDTO dto);
    void deleteCouncil(Long id);
    void assignTopicsToCouncil(Long councilId, List<Long> topicIds);
    ReviewCouncilDTO getCouncilById(Long id);
    List<ReviewCouncilDTO> getCouncilsByPeriod(Long periodId);
    List<ReviewCouncilDTO> getCouncilsByTeacher(Long teacherId);
    List<ReviewCouncilDTO> getAllCouncils();
}
