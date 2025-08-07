package com.airoom.airoom.exam.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.exam.entity.value.RetryType;
import com.airoom.airoom.exam.entity.value.Subject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE EXAM SET DELETED_AT = NOW() WHERE EXAM_NO = ?")
@AllArgsConstructor
public class Exam extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examNo;

    @Column(nullable = false)
    private Integer examProblemCount;

    private LocalDateTime examStartTime;

    private LocalDateTime examEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RetryType examRetryType = RetryType.N;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Subject examSubject = Subject.MATH;

    @PrePersist
    public void prePersist() {
        if (examRetryType == null) {
            examRetryType = RetryType.N;
        }
        if (examSubject == null) {
            examSubject = Subject.MATH;
        }
    }
}
