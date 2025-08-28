package com.airoom.airoom.chat.controller;

import com.airoom.airoom.chat.model.dto.*;
import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Chat 관련 API", description = "Chat 관련 API")
public interface ChatRestSwagger {
    @Operation(
            summary = "채팅방 생성 및 조회 API",
            description = "채팅방을 생성하거나 조회합니다."
    )@PostMapping("/rooms/open")
    public Long openRoom(@RequestBody ChatRoomRequest chatRoomRequest);

    @Operation(
            summary = "대화목록 조회 API",
            description = "학생들과의 대화목록을 조회합니다."
    )@GetMapping("/rooms/list/{classroomTeacherNo}")
    public List<ChatRoomResponse> getChatRooms(@PathVariable Long classroomTeacherNo);

    @Operation(
            summary = "학생목록 조회 API",
            description = "클래스룸의 학생목록을 조회합니다."
    )@GetMapping("/students/list/{classroomNo}")
    public List<ClassroomStudentResponse> getChatStudents(@PathVariable Long classroomNo);

    @Operation(
            summary = "교사  조회 API",
            description = "클래스룸의 학생목록을 조회합니다."
    )@GetMapping("/teacher/{classroomNo}")
    public Long getTeacherNo(@PathVariable Long classroomNo);

    @Operation(
            summary = "채팅 내역 조회 API",
            description = "채팅 내역을 무한스크롤로 조회합니다."
    )@PostMapping("/rooms/messages")
    public List<ChatMessageResponse> messages(@RequestBody ScrollRequest scrollRequest);

    @Operation(
            summary = "채팅 알림 API",
            description = "읽지 않은 채팅 알림을 조회합니다."
    )@PostMapping("/unread/total")
    public UnreadResponse getTotalUnread(@RequestBody UnreadRequest unreadRequest);
}
