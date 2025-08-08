package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonNo;
    private String lessonName;
    private Integer lessonStart;
    private Integer lessonEnd;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_NO", nullable = false)
    private Unit unit;
}
