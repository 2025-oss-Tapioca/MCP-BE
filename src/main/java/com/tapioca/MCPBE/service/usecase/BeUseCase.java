package com.tapioca.MCPBE.service.usecase;

import com.fasterxml.jackson.databind.JsonNode;

public interface BeUseCase {
    public Object beRequest(JsonNode beJson);
}
