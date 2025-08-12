package com.tapioca.MCPBE.service.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.domain.dto.result.CodeSkeletonResultDto;

public interface CodeSkeletonUseCase {
    public CodeSkeletonResultDto execute(String teamCode);
}
