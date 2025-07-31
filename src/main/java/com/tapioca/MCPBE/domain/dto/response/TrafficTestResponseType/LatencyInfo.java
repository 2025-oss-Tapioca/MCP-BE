package com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType;

public record LatencyInfo(String mean, String p50, String p95, String p99, String max) {}
