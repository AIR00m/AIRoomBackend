package com.airoom.airoom.board.model.dto.assign;

import com.airoom.airoom.board.entity.Attachment;
import com.airoom.airoom.board.entity.BoardType;

import java.time.LocalDateTime;
import java.util.List;

public record AssignHomeworkAllResponse(
        Long assignBoardNo,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String content,
        boolean isGroupAssignType,
        Long homeworkBoardNo,
        String homeworkBoardContent,
        List<Attachment> teacherAttachment,
        List<Attachment> studentAttachment

) {
    //    public AssignHomeworkAllResponse (AssignResponse response,
//                                             List<Attachment> teacher,
//                                             List<Attachment> student){
//        this.assignBoardNo = response.assignBoardNo();
//        this.title = response.title();
//        this.startTime = response.startTime();
//        this.endTime = response.endTime();
//        this.content = response.content();
//        this.isGroupAssignType = response.isGroupAssignType();
//        this.homeworkBoardNo = response.homeworkBoardNo();
//        this.homeworkBoardContent = response.homeworkBoardContent();
//        this.teacherAttachment = teacher;
//        this.studentAttachment = student;

    public AssignHomeworkAllResponse(AssignResponse response,
                                     List<Attachment> teacher,
                                     List<Attachment> student) {
        this(
                response.assignBoardNo(),
                response.title(),
                response.startTime(),
                response.endTime(),
                response.content(),
                response.isGroupAssignType(),
                response.homeworkBoardNo(),
                response.homeworkBoardContent(),
                teacher,
                student
        );
    }
}
