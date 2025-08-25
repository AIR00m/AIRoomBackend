package com.airoom.airoom.board.model.service;

import com.airoom.airoom.board.entity.GroupBoard;
import com.airoom.airoom.board.model.dto.group.GroupBoardRequest;
import com.airoom.airoom.board.model.dto.group.GroupBoardsResponse;
import com.airoom.airoom.board.model.dto.group.GroupBoardResponse;
import com.airoom.airoom.board.model.repository.GroupBoardRepository;
import com.airoom.airoom.classroom.entity.Classroom;
import com.airoom.airoom.classroom.entity.ClassroomGroup;
import com.airoom.airoom.classroom.model.repository.ClassroomGroupRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomRepository;
import com.airoom.airoom.member.entity.Member;
import com.airoom.airoom.member.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupBoardService {
    private final GroupBoardRepository groupBoardRepository;
    private final MemberRepository memberRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomGroupRepository classroomGroupRepository;

    @Transactional
    public List<GroupBoardsResponse> getGroupBoardByGroupNo(Long groupNo) {
       return groupBoardRepository.getGroupBoardByGroupNo(groupNo);
    }

    public void saveGroupBoard (Long groupNo, Long classroomNo, String userId, GroupBoardRequest request) {
        Member writer = memberRepository.findMemberByMemberId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 아이디 입니다."));

        ClassroomGroup group = classroomGroupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹 입니다."));

        Classroom classroom = classroomRepository.findById(classroomNo)
                .orElseThrow(()-> new IllegalArgumentException("해당하는 클래스룸은 존재하지 않습니다."));

        GroupBoard board = GroupBoard.builder()
                .classroomGroup(group)
                .classroom(classroom)
                .member(writer)
                .groupBoardTitle(request.groupBoardTitle())
                .groupBoardContent(request.groupBoardContent())
                .build();

        groupBoardRepository.save(board);
    }

    public GroupBoardResponse getGroupBoardByGroupNoAndGroupBoardNo (Long groupBoardNo) {
        return groupBoardRepository.getGroupBoardByGroupNoAndGroupBoardNo(groupBoardNo);
    }


}
