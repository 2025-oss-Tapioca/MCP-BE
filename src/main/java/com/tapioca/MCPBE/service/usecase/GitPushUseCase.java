package com.tapioca.MCPBE.service.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.domain.dto.result.GitPushResultDto;

public interface GitPushUseCase {
    public GitPushResultDto execute(String teamCode);
}
