package com.airoom.airoom.subjectboard.controller;

import com.airoom.airoom.subjectboard.model.dto.SubjectBoardRequest;
import com.airoom.airoom.subjectboard.model.dto.SubjectBoardListResponse;
import com.airoom.airoom.subjectboard.model.service.SubjectBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subject-board")
public class SubjectBoardController implements SubjectBoardSwagger {

    private final SubjectBoardService subjectBoardService;

    @GetMapping
    public ResponseEntity<List<SubjectBoardListResponse>> findAllSubjectBoard() {
        return ResponseEntity.ok().body(subjectBoardService.findAll());
    }

    @PostMapping
    public ResponseEntity<Long> insertSubjectBoard(@RequestBody SubjectBoardRequest request) {
        Long boardNo = subjectBoardService.insertSubjectBoard(request);
        return ResponseEntity.ok(boardNo);
    }

    @PutMapping("/{subjectBoardNo}")
    public ResponseEntity<Void> updateSubjectBoard(@RequestBody SubjectBoardRequest request,
                                                   @PathVariable Long subjectBoardNo) {
        try {
            subjectBoardService.updateSubjectBoard(subjectBoardNo, request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{subjectBoardNo}")
    public ResponseEntity<Void> deleteSubjectBoard(@PathVariable Long subjectBoardNo) {
        try {
            subjectBoardService.deleteSubjectBoard(subjectBoardNo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
