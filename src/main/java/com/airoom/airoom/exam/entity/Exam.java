package com.airoom.airoom.exam.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import com.airoom.airoom.exam.entity.value.RetryType;
import com.airoom.airoom.exam.entity.value.Subject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.sql.Timestamp;

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

    private Timestamp examStartTime;

    private Timestamp examEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RetryType examRetryType = RetryType.N;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subject examSubject;

    @PrePersist
    public void prePersist() {
        if (examRetryType == null) {
            examRetryType = RetryType.N;
        }
    }
}
