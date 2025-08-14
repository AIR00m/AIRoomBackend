package com.airoom.airoom.exam.entity;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
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
@SQLDelete(sql = "UPDATE STUDENT_ANSWER SET DELETED_AT = NOW() WHERE SA_NO = ?")
@AllArgsConstructor
public class StudentAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saNo; //학생응답 고유번호

    @Column(nullable = false)
    private String saAnswer; //학생응답제출답안

    @Column(nullable = false)
    private boolean saIsCorrect; //정답여부

    @Column(nullable = false)
    private LocalDateTime saSolvingTime; //풀이시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CEP_NO")
    private CreatedExamProblem createdExamProblem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASSROOM_STUDENT_NO")
    private ClassroomStudent classroomStudent;
}
