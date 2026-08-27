package com.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"topic_id", "teacher_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "score_value", nullable = false, precision = 4, scale = 2)
    private BigDecimal scoreValue;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "scored_at", nullable = false, updatable = false)
    private LocalDateTime scoredAt;

    @PrePersist
    protected void onCreate() {
        this.scoredAt = LocalDateTime.now();
    }
}
