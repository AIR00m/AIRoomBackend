package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unitNo;
    private String unitTitle;
    private Integer unitNum;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEXTBOOK_NO", nullable = false)
    private Textbook textbook;
}
