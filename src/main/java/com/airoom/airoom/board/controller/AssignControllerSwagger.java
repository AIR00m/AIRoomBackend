package com.airoom.airoom.board.controller;

import com.airoom.airoom.board.model.dto.assign.AssignCreateRequest;
import com.airoom.airoom.board.model.dto.assign.AssignListResponse;
import com.airoom.airoom.common.value.MemberRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Assign 관련 API", description = "Assign 관련 API")
public interface AssignControllerSwagger {

    @Operation(
            summary = "과제 게시판 생성 API",
            description = "과제 게시판에 과제를 추가"
    )
    public ResponseEntity<String> createAssignment(@RequestBody AssignCreateRequest request);

    @Operation(
            summary = "과제 게시판 전체 조회",
            description = "해당 회원의 전체 과제를 조회"
    )
    public List<AssignListResponse> getAllAssignments(
            @PathVariable Long classroomNo,
            @RequestParam MemberRole userType,
            @RequestParam(required = false) Long classroomStudentNo);

}
