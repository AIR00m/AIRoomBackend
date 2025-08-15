package com.airoom.airoom.board.entity;

import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.Group;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

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

    @Column(nullable = false)
    private String GroupBoardTitle;
    // 모둠 게시판 제목

    @ManyToOne
    @JoinColumn(name = "CLASSROOM_NO")
    private Group group;
    // 클래스룸 모둠 고유 번호

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ATTACH_NO")
    private Attachment attachment;
    // 한 개의 GroupBoard 당 파일 1개를 위해서 unique
    // 단일 파일)

}
