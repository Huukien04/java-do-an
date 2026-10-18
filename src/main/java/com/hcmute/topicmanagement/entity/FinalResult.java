package com.hcmute.topicmanagement.entity;

import com.hcmute.topicmanagement.enums.EvaluationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "final_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false, unique = true)
    private Topic topic;

    @Column(name = "final_score", precision = 4, scale = 2)
    private BigDecimal finalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_status", length = 20)
    private EvaluationStatus evaluationStatus;

    @Column(name = "chairman_comments", columnDefinition = "TEXT")
    private String chairmanComments;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean isPublished = false;

    @Column(name = "chairman_confirmed", nullable = false)
    @Builder.Default
    private boolean chairmanConfirmed = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
