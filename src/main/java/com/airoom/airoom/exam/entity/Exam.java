package com.airoom.airoom.exam.entity;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE exam SET deleted_at = NOW() WHERE exam_no = ?")
@AllArgsConstructor
public class Exam extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examNo; //시험 고유번호

    @Column(nullable = false)
    private String examName; //시험 이름

    @Column(nullable = false)
    private Integer examProblemCount; //시험 문제수

    private LocalDateTime examStartTime; //시험 시작시간

    private LocalDateTime examEndTime; //시험 종료시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_no")
    private Classroom classroom; //클래스룸 고유번호

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "exam")
    @Builder.Default
    private List<ExamUnit> examUnitList = new ArrayList<>(); //시험 단원

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "exam")
    @Builder.Default
    private List<CreatedExamProblem> createdExamProblemList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "exam")
    @Builder.Default
    private List<StudentExam> studentExamList = new ArrayList<>();

    public void addExamUnit(ExamUnit examUnit) {
        if (examUnit != null) {
            examUnit.getExam().removeExamUnit(examUnit);
            examUnitList.add(examUnit);
            examUnit.setExam(this);
        }
    }

    public void removeExamUnit(ExamUnit examUnit) {
        if (examUnit != null && examUnitList.remove(examUnit)) {
            examUnit.setExam(null);
        }
    }

    public void addCreatedExamProblem(CreatedExamProblem cep) {
        if (cep != null) {
            cep.getExam().removeCreatedExamProblem(cep);
            createdExamProblemList.add(cep);
            cep.setExam(this);
        }
    }

    public void removeCreatedExamProblem(CreatedExamProblem cep) {
        if (cep != null && createdExamProblemList.remove(cep)) {
            cep.setExam(null);
        }
    }

    public void addStudentExam(StudentExam studentExam) {
        if (studentExam != null) {
            studentExam.getExam().removeStudentExam(studentExam);
            studentExamList.add(studentExam);
            studentExam.setExam(this);
        }
    }

    public void removeStudentExam(StudentExam studentExam) {
        if (studentExam != null && studentExamList.remove(studentExam)) {
            studentExam.setExam(null);
        }
    }

}
