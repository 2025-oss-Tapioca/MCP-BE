package com.tapioca.MCPBE.domain.dto.result;

import java.util.Map;

public record McpLogRegisterRequestDto(
        String type,
        String internalKey,
        String callbackUrl,
        String teamCode,
        Map<String,Object> cfg
) {}
