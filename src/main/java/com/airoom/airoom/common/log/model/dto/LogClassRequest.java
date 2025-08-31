/*
  Created by IntelliJ IDEA.
  User: poj23
  Date: 25. 9. 1.
  Time: 오전 3:59
*/
package com.airoom.airoom.common.log.model.dto;

public record LogClassRequest(
        Long unitNo,
        Long classroomStudentNo,
        String llStartTime,
        String llEndTime,
        Long llDurationSec,
        String llType
) {

}
