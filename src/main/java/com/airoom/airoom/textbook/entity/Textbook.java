package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Textbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TEXTBOOK_NO")
    private Long textbookNo;
    @Enumerated(EnumType.STRING)
    private SUBJECT textbookSubject;
}
