package com.airoom.airoom.exam.entity;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE student_answer SET deleted_at = NOW() WHERE sa_no = ?")
@AllArgsConstructor
public class StudentAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saNo; //학생응답 고유번호

    @Column(nullable = false)
    private String saAnswer; //학생응답제출답안

    @Column(nullable = false)
    private boolean saIsCorrect; //정답여부

    private Duration saSolvingTime; //풀이시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cep_no")
    private CreatedExamProblem createdExamProblem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_student_no")
    private ClassroomStudent classroomStudent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_no")
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ep_no")
    private ExamProblem examProblem;


}
