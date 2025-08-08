package com.airoom.airoom.textbook.entity;

import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "DRAWING")
@SequenceGenerator(name = "seqDrawingNo", sequenceName = "DRAWING_SEQ", initialValue = 1, allocationSize = 1)
public class Drawing extends BaseEntity {
    @Id
    @GeneratedValue(generator = "seqDrawingNo",strategy = GenerationType.SEQUENCE)
    @Column(name = "DRAWING_NO")
    private Long drawingNo;
    @Column(name = "DRAWING_DATA")
    private String drawingData;
    @Column(name = "DRAWING_PAGE")
    private String drawingPage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEXTBOOK_NO", nullable = false)
    private Textbook textbook;
}