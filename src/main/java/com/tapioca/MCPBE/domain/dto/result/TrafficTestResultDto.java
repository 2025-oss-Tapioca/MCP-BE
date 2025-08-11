package com.tapioca.MCPBE.domain.dto.result;

import com.fasterxml.jackson.databind.JsonNode;

public record TrafficTestResultDto(
        JsonNode requests,       // 요청 수, rate, 처리율
        JsonNode bytes           // 전송된 데이터 크기 (in/out)
) {
}