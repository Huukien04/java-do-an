package com.hcmute.topicmanagement.entity;

import com.hcmute.topicmanagement.enums.AdvisorRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topic_advisors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"topic_id", "teacher_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicAdvisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "advisor_role", nullable = false, length = 20)
    @Builder.Default
    private AdvisorRole advisorRole = AdvisorRole.PRIMARY;
}
