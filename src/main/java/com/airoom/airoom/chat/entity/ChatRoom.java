package com.airoom.airoom.chat.entity;

import com.airoom.airoom.classroom.entity.ClassroomStudent;
import com.airoom.airoom.classroom.entity.ClassroomTeacher;
import com.airoom.airoom.common.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@SQLRestriction("DELETED_AT IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE CHAT_ROOM SET DELETED_AT = NOW() WHERE CR_NO = ?")
@AllArgsConstructor
/**
 * 채팅방 엔티티
 */
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long crNo; //채팅방 고유번호

    private String lastMessage; //채팅방 마지막메시지

    private LocalDateTime lastMessageTime; //채팅방 마지막메시지 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASSROOM_TEACHER_NO")
    private ClassroomTeacher classroomTeacher; //클래스룸 교사

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASSROOM_STUDENT_NO")
    private ClassroomStudent classroomStudent; //클래스룸 학생
}
