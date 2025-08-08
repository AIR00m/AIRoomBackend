package com.airoom.airoom.textbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "UNIT")
@SequenceGenerator(name = "seqUnitNo", sequenceName = "UNIT_SEQ", initialValue = 1, allocationSize = 1)
public class Unit {
    @Id
    @GeneratedValue(generator = "seqUnitNo",strategy = GenerationType.SEQUENCE)
    @Column(name = "UNIT_NO")
    private Long unitNo;
    @Column(name = "UNIT_TITLE")
    private String unitTitle;
    @Column(name = "UNIT_NUM")
    private Integer unitNum;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEXTBOOK_NO", nullable = false)
    private Textbook textbook;
}
