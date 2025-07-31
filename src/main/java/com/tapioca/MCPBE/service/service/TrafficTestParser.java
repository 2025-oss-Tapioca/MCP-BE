package com.tapioca.MCPBE.service.service;

import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseDto;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.ByteDetail;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.ByteInfo;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.DurationInfo;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.LatencyInfo;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseType.RequestInfo;

import java.util.HashMap;
import java.util.Map;

public class TrafficTestParser {
    public static TrafficTestResponseDto parse(String rawOutput) {
        String[] lines = rawOutput.split("\\r?\\n");
        RequestInfo requestInfo = null;
        DurationInfo durationInfo = null;
        LatencyInfo latencyInfo = null;
        ByteDetail inBytes = null;
        ByteDetail outBytes = null;
        String successRatio = null;
        Map<String, Integer> statusCodes = new HashMap<>();

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Requests")) {
                String[] parts = extractValues(line);
                requestInfo = new RequestInfo(
                        Integer.parseInt(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2])
                );
            } else if (line.startsWith("Duration")) {
                String[] parts = extractValues(line);
                durationInfo = new DurationInfo(parts[0], parts[1], parts[2]);
            } else if (line.startsWith("Latencies")) {
                String[] parts = extractValues(line);
                latencyInfo = new LatencyInfo(parts[0], parts[1], parts[2], parts[3], parts[4]);
            } else if (line.startsWith("Bytes In")) {
                String[] parts = extractValues(line);
                inBytes = new ByteDetail(Integer.parseInt(parts[0]), Double.parseDouble(parts[1]));
            } else if (line.startsWith("Bytes Out")) {
                String[] parts = extractValues(line);
                outBytes = new ByteDetail(Integer.parseInt(parts[0]), Double.parseDouble(parts[1]));
            } else if (line.startsWith("Success")) {
                successRatio = line.split("\\s+")[line.split("\\s+").length - 1];
            } else if (line.startsWith("Status Codes")) {
                String codePart = line.substring(line.indexOf("]") + 1).trim(); // e.g., "200:3"
                String[] tokens = codePart.split(",");
                for (String token : tokens) {
                    String[] kv = token.trim().split(":");
                    statusCodes.put(kv[0], Integer.parseInt(kv[1]));
                }
            }
        }

        return new TrafficTestResponseDto(
                requestInfo,
                durationInfo,
                latencyInfo,
                new ByteInfo(inBytes, outBytes),
                successRatio,
                statusCodes
        );
    }

    private static String[] extractValues(String line) {
        return line.substring(line.indexOf("]") + 1).trim().split(",\\s*");
    }
}
