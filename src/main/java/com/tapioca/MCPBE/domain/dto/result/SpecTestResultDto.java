package com.tapioca.MCPBE.domain.dto.result;

import com.fasterxml.jackson.databind.JsonNode;

public record SpecTestResultDto(
        JsonNode latencies,      // 요청 응답 시간 분포
        JsonNode duration,       // 테스트 시간 관련 정보
        double throughput,       // 초당 성공 응답 수
        String successRatio,     // 요청 성공률 (%)
        JsonNode statusCodes     // 응답 코드 분포 (예: 200, 500 등)
) {
}
