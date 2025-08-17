package com.airoom.airoom.notification.entity.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    //동적인

    //학생 알림
    NEW_MATERIAL_UPLOADED("새로운 학습 자료가 등록되었습니다"),

    NEW_LESSON_UPLOADED("새로운 학습이 등록되었습니다"),

    NEW_ASSIGNMENT_UPLOADED("새로운 과제가 등록되었습니다"),
    
    NEW_EXAM("새로운 평가가 생성되었습니다"),
    
    NEW_NOTICE("새로운 공지사항이 등록되었습니다"),
    
    ASSIGNMENT_UPDATED("과제가 수정되었습니다"),
    
    ASSIGNMENT_GRADED("과제가 채점 되었습니다"),
    
    //공통 알림

    ASSIGNMENT_D_7("마감이 7일 남은 과제가 있습니다"),

    ASSIGNMENT_D_1("마감이 1일 남은 과제가 있습니다"),

    ASSIGNMENT_D_DAY("과제 마감일 입니다"),

    EXAM_D_7("마감이 7일 남은 평가가 있습니다"),

    EXAM_D_1("마감이 1일 남은 평가가 있습니다"),

    EXAM_D_DAY("평가 마감일 입니다"),


    //강사 알림
    
    ASSIGNMENT_SUBMITTED("학생이 과제를 제출했습니다"),

    ASSIGNMENT_RESUBMITTED("학생이 과제를 수정하여 제출했습니다"),

    NEW_GROUP_POST("모둠 게시판에 새 글이 작성되었습니다"),

    NEW_GROUP_COMMENT("모둠 게시판에 새 댓글이 작성되었습니다");

    private final String msg;

    public static NotificationType fromValue(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.msg.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("올바른 NotificationType 값이 아닙니다"+value);
    }


}

