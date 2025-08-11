package com.tapioca.MCPBE.domain.dto.result;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.domain.entity.erd.ErdEntity;

public record CodeSkeletonResultDto(
        String teamCode,
        ErdEntity teamErd
) {
}
