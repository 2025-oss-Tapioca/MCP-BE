package com.tapioca.MCPBE.domain.dto.request;

public record LogLineDto(
        String timestamp,
        String level,
        String service,
        String message
) {}
