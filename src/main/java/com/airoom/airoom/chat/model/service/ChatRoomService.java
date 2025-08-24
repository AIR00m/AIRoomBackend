package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.entity.ChatRoom;
import com.airoom.airoom.chat.model.dto.ChatRoomResponse;
import com.airoom.airoom.chat.model.repository.ChatRoomRepository;
import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.classroom.model.repository.ClassroomStudentRepository;
import com.airoom.airoom.classroom.model.repository.ClassroomTeacherRepository;
import com.airoom.airoom.common.value.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ClassroomTeacherRepository teacherRepository;
    private final ClassroomStudentRepository studentRepository;

    @Transactional
    public ChatRoom getOrCreateRoom(Long classroomTeacherNo, Long classroomStudentNo) {
        ClassroomTeacher classroomTeacher= teacherRepository.findById(classroomTeacherNo)
                .orElseThrow();
        ClassroomStudent classroomStudent= studentRepository.findById(classroomStudentNo)
                .orElseThrow();
        return chatRoomRepository.findByClassroomTeacherAndClassroomStudent(classroomTeacher, classroomStudent)
                .orElseGet(() -> {
                    ChatRoom room = ChatRoom.builder()
                            .classroomTeacher(classroomTeacher)
                            .classroomStudent(classroomStudent)
                            .lastMessage(null)
                            .lastMessageTime(null)
                            .build();
                    return chatRoomRepository.save(room);
                });
    }

    // 방 참여자 검증 (구독/발행 인터셉터에서 사용)
    public boolean isParticipant(Long roomId, Long memberNo, MemberRole role) {
        return chatRoomRepository.findById(roomId).map(r -> switch (role) {
            case TEACHER -> Objects.equals(r.getClassroomTeacher().getTeacher().getMemberNo(), memberNo);
            case STUDENT -> Objects.equals(r.getClassroomStudent().getStudent().getMemberNo(), memberNo);
        }).orElse(false);
    }

    public List<ChatRoomResponse> getChatRooms(Long classroomTeacherNo) {
        ClassroomTeacher classroomTeacher= teacherRepository.findById(classroomTeacherNo)
                .orElseThrow();
        return chatRoomRepository.findByClassroomTeacher(classroomTeacher).stream()
                .map(this::buildChatRoomResponse).toList();
    }

    //메소드 추출
    private ChatRoomResponse buildChatRoomResponse(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .crNo(chatRoom.getCrNo())
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .build();
    }
}
