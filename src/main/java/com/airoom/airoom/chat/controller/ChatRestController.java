package com.airoom.airoom.chat.controller;

import com.airoom.airoom.chat.entity.ChatMessage;
import com.airoom.airoom.chat.entity.ChatRoom;
import com.airoom.airoom.chat.model.dto.ChatMessageResponse;
import com.airoom.airoom.chat.model.dto.ChatRoomResponse;
import com.airoom.airoom.chat.model.service.ChatMessageProducer;
import com.airoom.airoom.chat.model.service.ChatMessageService;
import com.airoom.airoom.chat.model.service.ChatRoomService;
import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRestController {
    private final ChatRoomService roomService;
    private final ChatMessageService messageService;
    private final ClassroomService classroomService;
    private final ChatMessageProducer producer;

    // 교사: 학생과 대화하기 → 방 생성/조회
    @PostMapping("/rooms/open")
    public Long openRoom(@RequestParam Long classroomTeacherNo, @RequestParam Long classroomStudentNo) {
        return roomService.getOrCreateRoom(classroomTeacherNo, classroomStudentNo).getCrNo();
    }

    // 교사: 대화 목록
    @GetMapping("/rooms/list/{classroomTeacherNo}")
    public List<ChatRoomResponse> getChatRooms(@PathVariable Long classroomTeacherNo) {
        return roomService.getChatRooms(classroomTeacherNo);
    }
    //교사: 학생 목록
    @GetMapping("/students/list/{classroomNo}")
    public List<ClassroomStudentResponse> getChatStudents(@PathVariable Long classroomNo) {
        return classroomService.getClassroomStudentAll(classroomNo);
    }

    // 무한스크롤 메시지
    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageResponse> messages(@PathVariable Long roomId,
                                              @RequestParam(required=false) Long beforeId,
                                              @RequestParam(defaultValue="30") int size) {
        return messageService.scroll(roomId, beforeId, size);
    }
}
