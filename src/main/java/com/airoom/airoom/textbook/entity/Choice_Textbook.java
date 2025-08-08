package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Choice_Textbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer choiceNo;
    private String choiceGrade;
    private String choiceSchool;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEXTBOOK_NO", nullable = false)
    private Textbook textbook;
}
