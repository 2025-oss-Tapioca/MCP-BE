package com.tapioca.MCPBE.domain.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

public record VegetaTestRequestDto(
        String method,
        String url,
        String loginPath,
        String loginId,
        String password,
        int rate,
        int duration,
        JsonNode jsonBody // 요청 본문이 필요 없는 경우 null 가능
) {
}
