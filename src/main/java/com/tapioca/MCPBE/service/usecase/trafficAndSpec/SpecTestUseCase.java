package com.tapioca.MCPBE.service.usecase.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.domain.dto.result.SpecTestResultDto;

public interface SpecTestUseCase {
    public SpecTestResultDto execute(String method, String url, String loginPath, String loginId,
                                     String password, int rate, int duration, JsonNode jsonBody);
}
