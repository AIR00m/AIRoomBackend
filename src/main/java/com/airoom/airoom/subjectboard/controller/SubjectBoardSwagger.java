package com.airoom.airoom.subjectboard.controller;

import com.airoom.airoom.subjectboard.model.dto.SubjectBoardListResponse;
import com.airoom.airoom.subjectboard.model.dto.SubjectBoardRequest;
import com.airoom.airoom.subjectboard.model.dto.SubjectBoardViewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "SubjectBoard 관련 API", description = "SubjectBoard 관련 API")
public interface SubjectBoardSwagger {

    @Operation(
            summary = "SubjectBoard 리스트 조회 API",
            description = "SubjectBoard 리스트를 조회합니다."
    )
    public ResponseEntity<List<SubjectBoardListResponse>> getAllSubjectBoards(@PathVariable Long classNo);

    @Operation(
            summary = "SubjectBoard 상세 조회 API",
            description = "SubjectBoard 상세 조회합니다."
    )
    public ResponseEntity<SubjectBoardViewResponse> getSubjectBoard(@PathVariable Long boardNo);


    @Operation(
            summary = "SubjectBoard 등록 API",
            description = "SubjectBoard 및 첨부파일을 등록합니다."
    )
    public ResponseEntity<Long> insertSubjectBoard(
            @RequestBody SubjectBoardRequest request
    );

    @Operation(
            summary = "SubjectBoard 수정 API",
            description = "SubjectBoard 및 첨부파일을 수정합니다."
    )
    public ResponseEntity<Void> updateSubjectBoard(@RequestBody SubjectBoardRequest request,
                                                   @PathVariable Long subjectBoardNo);

    @Operation(
            summary = "SubjectBoard 삭제 API",
            description = "SubjectBoard 및 첨부파일을 삭제합니다."
    )
    public ResponseEntity<Void> deleteSubjectBoard(@PathVariable Long subjectBoardNo);


}
