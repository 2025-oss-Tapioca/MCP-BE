package com.tapioca.MCPBE.service.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseDto;

public interface ApiSpecTestUseCase {
    TrafficTestResponseDto execute(JsonNode json);
}
