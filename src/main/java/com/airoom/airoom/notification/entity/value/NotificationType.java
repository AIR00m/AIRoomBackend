package com.airoom.airoom.notification.entity.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum NotificationType {

    // 학생 알림 (과제/평가 관련)
    NEW_ASSIGNMENT("[과제] 새로운 과제가 출제되었습니다", "/assign/student/"),
    NEW_GROUP_ASSIGNMENT("[모둠과제] 새로운 과제가 출제되었습니다", "/assign/student/"),
    NEW_EXAM("[평가] 새로운 평가가 생성되었습니다", "/exam/student/"),
    NEW_MATERIAL("[학습자료] 새로운 학습 자료가 등록되었습니다", "/material/"),
    NEW_NOTICE("[공지] 새로운 공지사항이 등록되었습니다", "/notice/"),
    ASSIGNMENT_GRADED("[과제] 과제가 채점되었습니다", "/assign/student/"),

    // 선생님 알림
    ASSIGNMENT_SUBMITTED("[과제] 학생이 과제를 제출했습니다", "/assign/teacher/"),
    NEW_GROUP_POST("[모둠게시판] 새 글이 작성되었습니다", "/group/post/"),
    NEW_GROUP_COMMENT("[모둠게시판] 새 댓글이 작성되었습니다", "/group/post/"),

    // 공통 알림 (마감일 관련)
    ASSIGNMENT_D_7("[과제] 마감이 7일 남았습니다", "/assign/student/"),
    ASSIGNMENT_D_1("[과제] 마감이 1일 남았습니다", "/assign/student/"),
    ASSIGNMENT_D_DAY("[과제] 마감일입니다", "/assign/student/"),
    EXAM_D_7("[평가] 마감이 7일 남았습니다", "/exam/student/"),
    EXAM_D_1("[평가] 마감이 1일 남았습니다", "/exam/student/"),
    EXAM_D_DAY("[평가] 마감일입니다", "/exam/student/");


    private final String message;
    private final String location;

    NotificationType(String msg, String redirectLocation) {
        this.message = msg;
        this.location = PATH + redirectLocation;
    }

    private static final String PATH ="/airoom";

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

