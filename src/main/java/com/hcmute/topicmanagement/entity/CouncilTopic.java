package com.hcmute.topicmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "council_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_id", nullable = false)
    private ReviewCouncil council;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false, unique = true)
    private Topic topic;
}
