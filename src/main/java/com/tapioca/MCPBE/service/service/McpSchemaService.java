package com.tapioca.MCPBE.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.mcp.McpTools;
import com.tapioca.MCPBE.service.usecase.McpSchemaUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class McpSchemaService implements McpSchemaUseCase {

    private final McpTools tools;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> buildSchema() {
        // @Tool 메서드들을 ToolCallback[]로 수집
        ToolCallback[] callbacks = ToolCallbacks.from(tools);

        List<Map<String, Object>> toolList = new ArrayList<>(callbacks.length);

        for (ToolCallback cb : callbacks) {
            var def = cb.getToolDefinition();

            // Spring AI가 생성한 입력 스키마(JSON 문자열)
            var inputSchemaJson = def.inputSchema();

            Object parametersAsMap = parseJsonOrEmpty(inputSchemaJson);

            Map<String, Object> toolObj = new LinkedHashMap<>();
            toolObj.put("name", def.name());
            toolObj.put("description", def.description());
            toolObj.put("parameters", parametersAsMap); // 완성된 JSON 스키마 그대로

            toolList.add(toolObj);
        }

        return Map.of("tools", toolList);
    }

private Object parseJsonOrEmpty(String json) {
    if (json == null || json.isBlank()) {
        return Collections.emptyMap();
    }
    try {
        // 스키마는 객체(JSON Object)일 것이므로 Map으로 파싱
        return objectMapper.readValue(json, Map.class);
    } catch (JsonProcessingException e) {
        // 파싱 실패 시 원문 문자열을 그대로 넣거나 빈맵 반환 (운영선호도에 맞춰 선택)
        return Collections.emptyMap();
    }
}
}
