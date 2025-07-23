package com.tapioca.MCPBE.domain.dto.gpt;

import com.fasterxml.jackson.databind.JsonNode;

public record GptResponseDto(
        String type,
        JsonNode data
) {
}
