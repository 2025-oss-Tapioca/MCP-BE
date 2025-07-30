package com.tapioca.MCPBE.domain.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

public record BeRequestDto(
        JsonNode beRequest
) {
}
