package com.tapioca.MCPBE.domain.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

public record TestVegetaRequestDto(
        String method, String url, String loginPath,String loginId,
        String password, int rate, int duration, JsonNode jsonBody
) {
}
