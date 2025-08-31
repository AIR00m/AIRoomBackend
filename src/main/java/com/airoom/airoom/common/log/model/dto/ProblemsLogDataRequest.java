/*
  Created by IntelliJ IDEA.
  User: poj23
  Date: 25. 9. 1.
  Time: 오전 2:19
*/
package com.airoom.airoom.common.log.model.dto;

public record ProblemsLogDataRequest(
        Integer problemNo,
        String selectedAnswer,
        Long llDurationSec,
        Long cepNo,
        Long unitNo,
        Integer anomalyCount
) {

}
