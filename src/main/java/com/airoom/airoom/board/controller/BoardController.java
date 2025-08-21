package com.airoom.airoom.board.controller;

import com.airoom.airoom.board.model.dto.*;
import com.airoom.airoom.board.model.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assignments")
@Slf4j
public class BoardController {
    private final BoardService boardService;

    /**
     * 테스트용 간단한 과제 생성 API
     * Vue.js에서 보낸 JSON을 Map으로 받아서 콘솔 출력
     */
    @PostMapping("/create")
    public ResponseEntity<String> createAssignment(@RequestBody AssignmentCreateRequest request) {
        // ✅ 받은 데이터 전체 출력
        try {
            boardService.createAssignment(request);
            return ResponseEntity.ok("✅ 과제 생성 요청을 성공적으로 받았습니다!");
        }catch (Exception e) {
            // ✅ Vue.js로 성공 메시지 전송
            return ResponseEntity.badRequest().body("과제생성 실패"+e.getMessage());

        }
    }

    /**
     * 과제 목록 조회 (학생/선생님 통합)
     */
    @GetMapping("/list/{classroomNo}")
    public List<AssignmentListResponseDto> getAllAssignments(
            @PathVariable Long classroomNo,
            @RequestParam String userType,
            @RequestParam(required = false) Long classroomStudentNo) { // memberNo → classroomStudentNo 변경

        if ("teacher".equals(userType)) {
            return boardService.getAssignmentsForTeacher(classroomNo);
        } else if ("student".equals(userType) && classroomStudentNo != null) {
            return boardService.getAssignmentsForStudent(classroomNo, classroomStudentNo); // 파라미터 변경
        } else {
            throw new IllegalArgumentException("Invalid parameters");
        }
    }

//
//    @GetMapping("/list/{assignBoardNo}")
//    public ResponseEntity<AssignmentDetailResponseDto> getAssignmentByAssignBoardNo(@PathVariable Long assignBoardNo) {
//        try {
//            log.info("과제 상세 조회 요청 - assignBoardNo: {}", assignBoardNo);
//            AssignmentDetailResponseDto assignment = boardService.getAssignmentByAssignBoardNo(assignBoardNo);
//            if (assignment == null) {
//                return ResponseEntity.notFound().build();
//            }
//            return ResponseEntity.ok(assignment);
//        } catch (Exception e) {
//            log.error("과제 상세 조회 실패 - assignBoardNo: {}, Error: {}", assignBoardNo, e.getMessage());
//            return ResponseEntity.internalServerError().build();
//        }
//    }

    /**
     * 과제 제출 API
     */
//    @PostMapping("/list/{id}/submit")
//    public ResponseEntity<String> submitAssignment(
//            @PathVariable Long id,
//            @RequestBody AssignmentSubmissionDto submissionDto) {
//        log.info("과제 제출 요청 - 과제 ID: {}, 제출 내용: {}", id, submissionDto.getContent());
//        boardService.submitAssignment(id, submissionDto);
//        return ResponseEntity.ok("🐥 과제가 성공적으로 제출되었습니다!");
//    }

}
