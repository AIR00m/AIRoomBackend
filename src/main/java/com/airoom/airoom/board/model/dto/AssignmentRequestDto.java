package com.airoom.airoom.board.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequestDto {

    private AssignBoardDto assignBoard;
    private List<AttachmentFileDto> attachmentFile;
    private List<AssignTargetDto> assignTargets;

    private boolean isGroupAssignment;
    private List<Long> selectedGroups;
    private boolean createGroupBoard;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignBoardDto {
        private String boardContent;
        private String boardType; // "ASSIGN"
        private Long memberNo;
        private Long classroomNo;
        private String assignBoardTitle;
        private String assignStart;
        private String assignEnd;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentFileDto {
        private String fileName;
        private String fileUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignTargetDto {
        private Long targetNo;
        private boolean groupAssignType;
    }
}
