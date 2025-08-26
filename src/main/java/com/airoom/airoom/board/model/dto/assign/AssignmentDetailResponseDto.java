package com.airoom.airoom.board.model.dto.assign;


import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDetailResponseDto {
    private Long assignBoardNo;
    private String title;
    private String content;
    private String startDate;
    private String dueDate;
    private boolean isGroupAssignment;
    private List<String> targetStudents;
    private List<GroupInfo> assignedGroups;
    private List<FileInfo> teacherAttachments;
    private SubmissionInfo mySubmission;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfo {
        private Long groupId;
        private String groupName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String name;
        private String url;
    }

    // 👇 내가 제출한 과제 내용(텍스트/파일/제출일 등)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionInfo {
        private String content;                  // 학생이 쓴 제출 본문
        private List<FileInfo> files;            // 첨부파일 리스트
        private String submissionDate;           // 제출 일시
        private String submitter;                // (optional) 제출자명 또는 ID
        private String feedback;                 // (optional) 피드백 말고 점수만!!!!!!!!!!!!!!!!!!!
        private String feedbackDate;             // (optional) 피드백 일자

        //이거 피드백은 평가가 완료되어야 출력해줄수있잖아
    }
}
