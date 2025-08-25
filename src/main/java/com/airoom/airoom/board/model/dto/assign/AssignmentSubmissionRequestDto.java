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
public class AssignmentSubmissionRequestDto {

    private String content;
    private List<FileSubmission> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileSubmission {
        private String name;
        private long size;
        private String type;
        // 실제로는 파일 업로드 처리 로직 필요
    }
}
