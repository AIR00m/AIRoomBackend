package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.entity.ChatMessage;
import com.airoom.airoom.chat.entity.ChatRoom;
import com.airoom.airoom.chat.model.dto.ChatMessageRequest;
import com.airoom.airoom.chat.model.dto.ChatMessageResponse;
import com.airoom.airoom.chat.model.repository.ChatMessageRepository;
import com.airoom.airoom.chat.model.repository.ChatRoomRepository;
import com.airoom.airoom.common.value.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatReadService readService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void saveMessage(ChatMessageRequest request) {
        ChatRoom room = chatRoomRepository.findById(request.getCrNo())
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .cmContent(request.getContent())
                .cmWriterType(request.getWriterRole())
                .readByTeacher(request.getWriterRole() == MemberRole.TEACHER)
                .readByStudent(request.getWriterRole() == MemberRole.STUDENT)
                .build();
        //DB저장
        ChatMessage savedMessage = chatMessageRepository.save(message);

        request.setSentAt(LocalDateTime.now());
        //채팅방 메타데이터
        room.updateCR(request.getContent(), request.getSentAt());

        MemberRole receiver = (request.getWriterRole() == MemberRole.TEACHER) ? MemberRole.STUDENT : MemberRole.TEACHER;
        //안읽은 메시지수 카운트
        readService.incrementUnread(request.getCrNo(), receiver);
        //메시지 브로드캐스트
        ChatMessageResponse dto = ChatMessageResponse.builder()
                .crNo(request.getCrNo())
                .messageId(savedMessage.getCmNo())
                .content(request.getContent())
                .writerRole(request.getWriterRole())
                .sentAt(request.getSentAt())
                .build();
        messagingTemplate.convertAndSend("/topic/chat/" + request.getCrNo(), dto);
        //알림 브로드캐스트
        sendUnreadNotification(request.getCrNo(), request.getWriterRole());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> scroll(Long roomId, Long beforeId) {
        int size = 10;
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
        Pageable p = PageRequest.of(0, size);
        List<ChatMessageResponse> list = (beforeId == null)
                ? chatMessageRepository.findByChatRoomOrderByCmNoDesc(room, p).stream()
                .map(this::buildChatMessageResponse).collect(Collectors.toList())
                : chatMessageRepository.findByChatRoomAndCmNoLessThanOrderByCmNoDesc(room, beforeId, p).stream()
                .map(this::buildChatMessageResponse).collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

    @Transactional
    public void deleteOne(Long cmNo) {
        chatMessageRepository.deleteById(cmNo);
    }

    private ChatMessageResponse buildChatMessageResponse(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .messageId(chatMessage.getCmNo())
                .crNo(chatMessage.getChatRoom().getCrNo())
                .content(chatMessage.getCmContent())
                .sentAt(chatMessage.getCreatedAt())
                .writerRole(chatMessage.getCmWriterType())
                .build();
    }

    private void sendUnreadNotification(Long roomId, MemberRole senderRole) {
        try {
            // 채팅방 정보 조회
            ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();

            // 상대방 결정 (보낸 사람이 아닌 상대방에게만 알림)
            if (senderRole == MemberRole.TEACHER) {
                // 선생님이 보낸 경우 → 학생에게 알림
                long unreadCount = readService.getTotalUnread(
                        room.getClassroomStudent().getClassRoomStudentNo(),
                        MemberRole.STUDENT
                );

                Map<String, Long> notification = Map.of("totalUnread", unreadCount);
                messagingTemplate.convertAndSend(
                        "/topic/unread/student/" + room.getClassroomStudent().getClassRoomStudentNo(),
                        notification
                );

            } else if (senderRole == MemberRole.STUDENT) {
                // 학생이 보낸 경우 → 선생님에게 알림
                long unreadCount = readService.getTotalUnread(
                        room.getClassroomTeacher().getClassroomTeacherNo(),
                        MemberRole.TEACHER
                );

                Map<String, Long> notification = Map.of("totalUnread", unreadCount);
                messagingTemplate.convertAndSend(
                        "/topic/unread/teacher/" + room.getClassroomTeacher().getClassroomTeacherNo(),
                        notification
                );
            }

        } catch (Exception e) {
            System.err.println("미읽음 알림 전송 실패: " + e.getMessage());
        }
    }
}
