package com.airoom.airoom.notification.entity.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum NotificationType {

    // 학생 알림 (과제/평가 관련)
    NEW_ASSIGNMENT("[과제] 새로운 과제가 출제되었습니다", "/assignment"),
    NEW_GROUP_ASSIGNMENT("[모둠과제] 새로운 과제가 출제되었습니다", "/assignment"),
    NEW_EXAM("[평가] 새로운 평가가 생성되었습니다", "/exam"),
    NEW_MATERIAL("[학습자료] 새로운 학습 자료가 등록되었습니다", "/subjectboard/list"),
    ASSIGNMENT_GRADED("[과제] 과제가 채점되었습니다", "/assignment"),

    // 선생님 알림
    ASSIGNMENT_SUBMITTED("[과제] 학생이 과제를 제출했습니다", "/assignment"),
    NEW_GROUP_POST("[모둠게시판] 새 글이 작성되었습니다", "/assignment/groupboard"),
    NEW_GROUP_COMMENT("[모둠게시판] 새 댓글이 작성되었습니다", "/assignment/groupboard");

    private final String message;
    private final String location;

    NotificationType(String msg, String redirectLocation) {
        this.message = msg;
        this.location = redirectLocation;
    }


    public String getMsg() {
        return message;
    }

    public String getRedirectLocation() {
        return location;
    }


//    public static NotificationType fromValue(String value) {
//        for (NotificationType type : NotificationType.values()) {
//            if (type.msg.equals(value)) {
//                return type;
//            }
//        }
//        throw new IllegalArgumentException("올바른 NotificationType 값이 아닙니다"+value);
//    }


}

