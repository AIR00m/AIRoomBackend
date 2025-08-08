package com.airoom.airoom.textbook.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Drawing extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drawingNo;
    private String drawingData;
    private String drawingPage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEXTBOOK_NO", nullable = false)
    private Textbook textbook;
}