package com.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topic_reviewers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"topic_id", "teacher_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicReviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
}
