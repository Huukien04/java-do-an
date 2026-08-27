package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.ScoreDTO;
import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.EvaluationStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScoreServiceImpl implements ScoreService {

    private final ScoreRepository scoreRepository;
    private final FinalResultRepository finalResultRepository;
    private final TopicRepository topicRepository;
    private final TeacherRepository teacherRepository;
    private final TopicAdvisorRepository topicAdvisorRepository;

    @Override
    public ScoreDTO submitScore(ScoreDTO dto, Long currentTeacherId) {
        Topic topic = topicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", dto.getTopicId()));

        Teacher teacher = teacherRepository.findById(currentTeacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên", "id", currentTeacherId));

        // R-SCORE-01: Teacher CANNOT score a topic they advise!
        boolean isAdvisor = topicAdvisorRepository.existsByTopicIdAndTeacherId(topic.getId(), currentTeacherId);
        if (isAdvisor) {
            throw new BusinessException("R-SCORE-01 VI PHẠM: Giảng viên không được phép chấm điểm cho Đề tài mà mình đang Hướng dẫn!");
        }

        // Check score range
        if (dto.getScoreValue().compareTo(BigDecimal.ZERO) < 0 || dto.getScoreValue().compareTo(BigDecimal.valueOf(10)) > 0) {
            throw new BusinessException("Điểm số phải nằm trong khoảng từ 0.0 đến 10.0.");
        }

        Optional<Score> existingOpt = scoreRepository.findByTopicIdAndTeacherId(topic.getId(), currentTeacherId);
        Score score;
        if (existingOpt.isPresent()) {
            score = existingOpt.get();
            score.setScoreValue(dto.getScoreValue());
            score.setComments(dto.getComments());
        } else {
            score = Score.builder()
                    .topic(topic)
                    .teacher(teacher)
                    .scoreValue(dto.getScoreValue())
                    .comments(dto.getComments())
                    .build();
        }

        score = scoreRepository.save(score);

        // Auto update final result calculation
        calculateAndSaveFinalResult(topic.getId(), null);

        return mapToDTO(score);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoreDTO> getScoresByTopic(Long topicId) {
        return scoreRepository.findByTopicId(topicId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FinalResult calculateAndSaveFinalResult(Long topicId, String chairmanComments) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", topicId));

        List<Score> scores = scoreRepository.findByTopicId(topicId);
        if (scores.isEmpty()) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (Score s : scores) {
            sum = sum.add(s.getScoreValue());
        }

        BigDecimal average = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        EvaluationStatus status = (average.compareTo(BigDecimal.valueOf(5.0)) >= 0) ? EvaluationStatus.PASSED : EvaluationStatus.FAILED;

        Optional<FinalResult> resultOpt = finalResultRepository.findByTopicId(topicId);
        FinalResult result;
        if (resultOpt.isPresent()) {
            result = resultOpt.get();
            result.setFinalScore(average);
            result.setEvaluationStatus(status);
            if (chairmanComments != null) {
                result.setChairmanComments(chairmanComments);
            }
        } else {
            result = FinalResult.builder()
                    .topic(topic)
                    .finalScore(average)
                    .evaluationStatus(status)
                    .chairmanComments(chairmanComments)
                    .isPublished(false)
                    .build();
        }

        return finalResultRepository.save(result);
    }

    @Override
    public void publishResults(Long periodId) {
        List<Topic> topics = topicRepository.findByPeriodId(periodId);
        for (Topic t : topics) {
            Optional<FinalResult> frOpt = finalResultRepository.findByTopicId(t.getId());
            if (frOpt.isPresent()) {
                FinalResult fr = frOpt.get();
                fr.setPublished(true);
                fr.setPublishedAt(LocalDateTime.now());
                finalResultRepository.save(fr);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FinalResult getFinalResultByTopic(Long topicId) {
        return finalResultRepository.findByTopicId(topicId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalResult> getPublishedFinalResults() {
        return finalResultRepository.findByIsPublishedTrue();
    }

    private ScoreDTO mapToDTO(Score score) {
        return ScoreDTO.builder()
                .id(score.getId())
                .topicId(score.getTopic().getId())
                .teacherId(score.getTeacher().getId())
                .teacherName(score.getTeacher().getUser().getFullName())
                .scoreValue(score.getScoreValue())
                .comments(score.getComments())
                .scoredAt(score.getScoredAt())
                .build();
    }
}
