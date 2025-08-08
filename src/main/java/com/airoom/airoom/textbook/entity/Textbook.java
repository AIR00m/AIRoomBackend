package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "TEXTBOOK")
@SequenceGenerator(name = "seqTextbookNo", sequenceName = "TEXTBOOK_SEQ", initialValue = 1, allocationSize = 1)
public class Textbook {
    @Id
    @GeneratedValue(generator = "seqTextbookNo",strategy = GenerationType.SEQUENCE)
    @Column(name = "TEXTBOOK_NO")
    private Long textbookNo;
    @Enumerated(EnumType.STRING)
    private SUBJECT textbookSubject;
}
