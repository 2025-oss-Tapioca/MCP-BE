package com.tapioca.MCPBE.domain.dto.response;

import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.ByteInfo;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.DurationInfo;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.LatencyInfo;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.RequestInfo;

import java.util.Map;

public record TrafficTestResponseDto(
        RequestInfo requests,
        DurationInfo duration,
        LatencyInfo latencies,
        ByteInfo bytes,
        String successRatio,
        Map<String, Integer> statusCodes
) {
}


