package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.ScoreDTO;
import com.hcmute.topicmanagement.entity.FinalResult;

import java.util.List;

public interface ScoreService {
    ScoreDTO submitScore(ScoreDTO dto, Long currentTeacherId);
    List<ScoreDTO> getScoresByTopic(Long topicId);
    FinalResult calculateAndSaveFinalResult(Long topicId, String chairmanComments);
    void publishResults(Long periodId);
    FinalResult getFinalResultByTopic(Long topicId);
    List<FinalResult> getPublishedFinalResults();
}
