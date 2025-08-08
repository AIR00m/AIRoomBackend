package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "LESSON")
@SequenceGenerator(name = "seqLessonNo", sequenceName = "LESSON_SEQ", initialValue = 1, allocationSize = 1)
public class Lesson {
    @Id
    @GeneratedValue(generator = "seqLessonNo", strategy = GenerationType.SEQUENCE)
    @Column(name = "LESSON_NO")
    private Long lessonNo;
    @Column(name = "LESSON_NAME")
    private String lessonName;
    @Column(name = "LESSON_START")
    private Integer lessonStart;
    @Column(name = "LESSON_END")
    private Integer lessonEnd;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_NO", nullable = false)
    private Unit unit;
}
