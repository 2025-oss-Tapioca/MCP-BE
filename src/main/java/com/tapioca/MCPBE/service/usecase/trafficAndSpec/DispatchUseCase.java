package com.tapioca.MCPBE.service.usecase.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;

public interface DispatchUseCase {
    public Object execute(JsonNode json);
}
