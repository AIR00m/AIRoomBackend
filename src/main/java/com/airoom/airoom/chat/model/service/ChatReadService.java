package com.airoom.airoom.chat.model.service;

import com.airoom.airoom.chat.model.dto.ChatReadRequest;
import com.airoom.airoom.chat.model.repository.ChatMessageRepository;
import com.airoom.airoom.chat.model.repository.ChatRoomRepository;
import com.airoom.airoom.common.value.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatReadService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final StringRedisTemplate redisTemplate;

    private String unreadKey(Long roomId, MemberRole role) {
        return "chat:unread:" + roomId + ":" + role.name();
    }

    //마지막 메시지 이하 읽음 처리
    @Transactional
    public void markAsRead(ChatReadRequest chatReadRequest) {
        if (chatReadRequest.getLastReadMessageId() == null) {
            return;
        }
        if (chatReadRequest.getReaderRole() == MemberRole.TEACHER) {
            chatMessageRepository.markReadByTeacher(chatReadRequest.getCrNo(), chatReadRequest.getLastReadMessageId());
        } else {
            chatMessageRepository.markReadByStudent(chatReadRequest.getCrNo(), chatReadRequest.getLastReadMessageId());
        }
        redisTemplate.opsForValue().set(unreadKey(chatReadRequest.getCrNo(), chatReadRequest.getReaderRole()), "0");
    }

    //새 메시지 저장 후 수신자 unread
    public void incrementUnread(Long crNo, MemberRole receiverRole) {
        redisTemplate.opsForValue().increment(unreadKey(crNo, receiverRole));
    }

    public long getUnread(Long roomId, MemberRole role) {
        String v = redisTemplate.opsForValue().get(unreadKey(roomId, role));
        if (v != null) {
            return Long.parseLong(v);
        }
        long c = (role == MemberRole.TEACHER)
                ? chatMessageRepository.countByChatRoomCrNoAndReadByTeacherFalse(roomId)
                : chatMessageRepository.countByChatRoomCrNoAndReadByStudentFalse(roomId);
        if (c > 0) redisTemplate.opsForValue().set(unreadKey(roomId, role), String.valueOf(c));
        return c;
    }

    public long getTotalUnread(Long classroomMemberNo, MemberRole role) {
        // 내가 속한 모든 방 가져오기
        List<Long> roomIds = (role == MemberRole.TEACHER)
                ? chatRoomRepository.findIdsByTeacher(classroomMemberNo)
                : chatRoomRepository.findIdsByStudent(classroomMemberNo);

        long total = 0;
        for (Long roomId : roomIds) {
            total += getUnread(roomId, role);
        }
        return total;
    }


}
