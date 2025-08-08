package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "CHOICE_TEXTBOOK")
@SequenceGenerator(name = "seqChoiceTextbook", sequenceName = "CHOICE_TEXTBOOK_SEQ", allocationSize = 1, initialValue = 1)
public class Choice_Textbook {
    @Id
    @GeneratedValue(generator = "seqChoiceTextbook",strategy = GenerationType.SEQUENCE)
    @Column(name = "CHOICE_NO")
    private Integer choiceNo;
    @Column(name = "CHOICE_GRADE")
    private String choiceGrade;
    @Column(name = "CHOICE_SCHOOL")
    private String choiceSchool;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEXTBOOK_NO", nullable = false)
    private Textbook textbook;
}
