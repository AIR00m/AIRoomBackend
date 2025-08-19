package com.airoom.airoom.board.controller;

import com.airoom.airoom.board.model.dto.AssignmentListDto;
import com.airoom.airoom.board.model.dto.AssignmentRequestDto;
import com.airoom.airoom.board.model.dto.AssignmentResponseDto;
import com.airoom.airoom.board.model.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<String> createAssignment(@RequestBody AssignmentRequestDto requestDto) {
        // ✅ 받은 데이터 전체 출력
        boardService.createAssignment(requestDto);
        // ✅ Vue.js로 성공 메시지 전송
        return ResponseEntity.ok("✅ 과제 생성 요청을 성공적으로 받았습니다!");
    }

    @GetMapping("/list")
    public List<AssignmentListDto> getAllAssignments() {
        return  boardService.getAllAssignments();

    }

}
