package com.airoom.airoom.board.controller;


import com.airoom.airoom.board.entity.BoardType;
import com.airoom.airoom.board.model.dto.assign.*;
import com.airoom.airoom.board.model.service.AssignService;
import com.airoom.airoom.common.value.MemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/assign")
@Slf4j
public class AssignController implements AssignControllerSwagger {


    private final AssignService assignService;

    /**
     * 테스트용 간단한 과제 생성 API
     * Vue.js에서 보낸 JSON을 Map으로 받아서 콘솔 출력
     */
    @PostMapping("/create")
    public ResponseEntity<Long> createAssignment(@RequestBody AssignCreateRequest request) {
        return ResponseEntity.ok(assignService.createAssignment(request));
    }


    /**
     * 과제 목록 조회 (학생/선생님 통합)
     */
    @Override
    @GetMapping("/list/{classroomNo}")
    public List<AssignListResponse> getAllAssignments(
            @PathVariable Long classroomNo,
            @RequestParam MemberRole userType,
            @RequestParam(required = false) Long classroomStudentNo) { // memberNo → classroomStudentNo 변경

        return assignService.getAssignmentsForClassUser(classroomNo, classroomStudentNo, userType);

    }

    /**
     * 과제 클릭시 (해당하는 게시판으로 이동)
     */
    @GetMapping("{assignBoardNo}/student/{classroomStudentNo}")
    public ResponseEntity<AssignHomeworkAllResponse> getAssignBoardByBoardNo(
            @PathVariable Long assignBoardNo,
            @PathVariable Long classroomStudentNo) {
        return ResponseEntity.ok().body(assignService.getAssignBoardByBoardNo(assignBoardNo,classroomStudentNo));
    }

    @GetMapping("/teacher/{boardNo}")
    public ResponseEntity<AssignWithHomeworksResponse> getAssignTeacherByBoardNo
            (@PathVariable Long boardNo, @RequestParam BoardType boardType) {
        return ResponseEntity.ok().body(
                assignService.getAssignBoardWithSubmissions
                        (boardNo, boardType)
        );
    }

}
