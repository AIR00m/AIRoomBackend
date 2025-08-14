package com.airoom.airoom.board.entity;

import com.airoom.airoom.classroom.entity.Classroom;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
// JPA는 기본 생성자로 생성 | 개발자의 무분별한 생성을 막기 위해
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
// Soft Delete 방식
@SQLDelete(sql = "UPDATE GroupBoard SET deleted_at = NOW() WHERE board_no = ?")
@SQLRestriction("deleted_at IS NULL")
public class GroupBoard extends Board{

    private String GroupBoardTitle;

    @ManyToOne
    @JoinColumn(name = "CLASSROOM_NO")
    private Classroom classroom;


}
