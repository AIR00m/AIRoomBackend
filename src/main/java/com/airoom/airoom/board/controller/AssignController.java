package com.airoom.airoom.board.controller;


import com.airoom.airoom.board.entity.BoardType;
import com.airoom.airoom.board.model.dto.assign.AssignCreateRequest;
import com.airoom.airoom.board.model.dto.assign.AssignListResponse;
import com.airoom.airoom.board.model.dto.assign.AssignResponse;
import com.airoom.airoom.board.model.dto.assign.AssignWithHomeworksResponse;
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
    public ResponseEntity<String> createAssignment(@RequestBody AssignCreateRequest request) {
        // ✅ 받은 데이터 전체 출력
            assignService.createAssignment(request);
            return ResponseEntity.ok("✅ 과제 생성 요청을 성공적으로 받았습니다!");

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

        return assignService.getAssignmentsForClassUser(classroomNo, classroomStudentNo,userType);

    }
    /**
     * 과제 클릭시 (해당하는 게시판으로 이동)
     */
    @GetMapping("/student/{assignBoardNo}")
    public ResponseEntity<AssignResponse> getAssignBoardByBoardNo(@PathVariable Long assignBoardNo) {
        return ResponseEntity.ok().body(assignService.getAssignBoardByBoardNo(assignBoardNo));
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
