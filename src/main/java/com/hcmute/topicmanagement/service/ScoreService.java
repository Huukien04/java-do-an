package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.ScoreDTO;
import com.hcmute.topicmanagement.dto.ScoreProgressDTO;
import com.hcmute.topicmanagement.entity.FinalResult;

import java.util.List;

public interface ScoreService {
    ScoreDTO submitScore(ScoreDTO dto, Long currentTeacherId);
    List<ScoreDTO> getScoresByTopic(Long topicId);
    ScoreProgressDTO getScoringProgress(Long topicId);
    FinalResult calculateAndSaveFinalResult(Long topicId, String chairmanComments);
    FinalResult confirmFinalResult(Long topicId, String chairmanComments, Long currentTeacherId);
    boolean canViewScoring(Long topicId, Long teacherId);
    boolean isChairman(Long topicId, Long teacherId);
    boolean isReadyToScore(Long topicId);
    List<Long> getReviewerTopicIds(Long teacherId);
    void assignReviewers(Long topicId, List<Long> teacherIds);
    void processScoringDeadlines();
    void publishResults(Long periodId);
    boolean publishResultsIfReady(Long periodId);
    void unpublishResults(Long periodId);

    byte[] exportResultsExcel(Long periodId);
    FinalResult getFinalResultByTopic(Long topicId);
    List<FinalResult> getPublishedFinalResults();
    List<FinalResult> getPublishedFinalResultsByPeriod(Long periodId);
}
