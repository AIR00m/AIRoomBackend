package com.airoom.airoom.board.model.repository;

import com.airoom.airoom.board.entity.GroupBoard;
import com.airoom.airoom.board.model.dto.group.GroupBoardsResponse;
import com.airoom.airoom.board.model.dto.group.GroupBoardResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupBoardRepository extends JpaRepository<GroupBoard, Long> {
    @Query(
            """
        SELECT new com.airoom.airoom.board.model.dto.group.GroupBoardsResponse(
        gb.groupBoardNo,
        gb.groupBoardTitle,
        gb.groupBoardContent,
        m.memberName,
        gb.createdAt
        )
        from GroupBoard gb
            join gb.member m
            where gb.classroomGroup.groupNo = :groupNo
        order by gb.createdAt desc
"""
    )
    List<GroupBoardsResponse> getGroupBoardByGroupNo (Long groupNo);

    @Query
            ("""
    SELECT gb
        FROM GroupBoard gb
            join gb.classroomGroup cg
                where gb.classroomGroup.groupNo = :groupNo
    """)
    List<GroupBoard> findAllByClassroomGroupNo(Long groupNo);

    @Query("""
    SELECT new com.airoom.airoom.board.model.dto.group.GroupBoardResponse(
        gb.groupBoardTitle,
        gb.groupBoardContent,
        m.memberName,
        gb.createdAt,
        a.s3Key,
        a.originalName
        )
    FROM GroupBoard gb
    JOIN gb.member m
    LEFT JOIN Attachment a
      ON a.boardNo = gb.groupBoardNo
     AND a.boardType = "GROUP"
    WHERE gb.groupBoardNo = :groupBoardNo
    """)
    GroupBoardResponse getGroupBoardByGroupNoAndGroupBoardNo (Long groupBoardNo);
}
