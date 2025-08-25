package com.airoom.airoom.chat.controller;

import com.airoom.airoom.chat.entity.ChatMessage;
import com.airoom.airoom.chat.entity.ChatRoom;
import com.airoom.airoom.chat.model.dto.ChatMessageResponse;
import com.airoom.airoom.chat.model.dto.ChatRoomRequest;
import com.airoom.airoom.chat.model.dto.ChatRoomResponse;
import com.airoom.airoom.chat.model.dto.ScrollRequest;
import com.airoom.airoom.chat.model.service.ChatMessageProducer;
import com.airoom.airoom.chat.model.service.ChatMessageService;
import com.airoom.airoom.chat.model.service.ChatRoomService;
import com.airoom.airoom.classroom.model.dto.ClassroomStudentResponse;
import com.airoom.airoom.classroom.model.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatRestController implements ChatRestSwagger{
    private final ChatRoomService roomService;
    private final ChatMessageService messageService;
    private final ClassroomService classroomService;
    private final ChatMessageProducer producer;

    // 교사: 학생과 대화하기 → 채팅방 생성/조회
    // 학생: 교사와의 채팅방 조회
    @PostMapping("/rooms/open")
    public Long openRoom(@RequestBody ChatRoomRequest chatRoomRequest) {
        return roomService.getOrCreateRoom(
                chatRoomRequest.getClassroomTeacherNo(),
                chatRoomRequest.getClassroomStudentNo()).getCrNo();
    }

    // 교사: 대화 목록
    @GetMapping("/rooms/list/{classroomTeacherNo}")
    public List<ChatRoomResponse> getChatRooms(@PathVariable Long classroomTeacherNo) {
        return roomService.getChatRooms(classroomTeacherNo);
    }
    // 교사: 학생 목록
    @GetMapping("/students/list/{classroomNo}")
    public List<ClassroomStudentResponse> getChatStudents(@PathVariable Long classroomNo) {
        return classroomService.getClassroomStudentAll(classroomNo);
    }

    //학생: 클래스룸 교사번호 조회
    @GetMapping("/teacher/{classroomNo}")
    public Long getTeacherNo(@PathVariable Long classroomNo) {
        return classroomService.getClassTeacherNoByClassRoomNo(classroomNo);
    }

    // 무한스크롤 메시지
    @PostMapping("/rooms/messages")
    public List<ChatMessageResponse> messages(@RequestBody ScrollRequest scrollRequest) {
        log.info("scroll request: roomId={}, beforeId={}", scrollRequest.getCrNo(), scrollRequest.getBeforeId());

        return messageService.scroll(scrollRequest.getCrNo(), scrollRequest.getBeforeId());
    }
}
