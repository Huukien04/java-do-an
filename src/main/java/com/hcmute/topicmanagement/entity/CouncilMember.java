package com.hcmute.topicmanagement.entity;

import com.hcmute.topicmanagement.enums.CouncilRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "council_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"council_id", "teacher_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_id", nullable = false)
    private ReviewCouncil council;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CouncilRole role = CouncilRole.MEMBER;
}
