package com.tapioca.MCPBE.service.service;

import com.tapioca.MCPBE.mcp.McpTools;
import com.tapioca.MCPBE.service.usecase.McpSchemaUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    @Override
    public Map<String, Object> buildSchema(){
        List<Map<String, Object>> toolList = new ArrayList<>();

        for (Method m : tools.getClass().getDeclaredMethods()) {
            Tool anno = m.getAnnotation(Tool.class);
            if (anno == null) continue;

            String name = (anno.name() != null && !anno.name().isBlank())
                    ? anno.name() : m.getName();

            Map<String, Object> toolObj = new LinkedHashMap<>();
            toolObj.put("name", name);
            toolObj.put("description", anno.description());

            Map<String, Object> props = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();

            for (Parameter p : m.getParameters()) {
                ToolParam tp = p.getAnnotation(ToolParam.class);

                Map<String, Object> prop = new LinkedHashMap<>();
                prop.put("type", mapJavaTypeToJsonType(p.getType()));
                if (tp != null && !tp.description().isBlank()) {
                    prop.put("description", tp.description());
                }
                props.put(p.getName(), prop);

                if (p.getType().isPrimitive()) required.add(p.getName());
            }

            Map<String, Object> paramsSchema = new LinkedHashMap<>();
            paramsSchema.put("type", "object");
            paramsSchema.put("properties", props);
            if (!required.isEmpty()) paramsSchema.put("required", required);

            toolObj.put("parameters", paramsSchema);
            toolList.add(toolObj);
        }

        return Map.of("tools", toolList);
    }

    private String mapJavaTypeToJsonType(Class<?> c) {
        if (c == String.class) return "string";
        if (c == int.class || c == Integer.class
                || c == long.class || c == Long.class
                || c == short.class || c == Short.class) return "integer";
        if (c == float.class || c == Float.class
                || c == double.class || c == Double.class) return "number";
        if (c == boolean.class || c == Boolean.class) return "boolean";
        return "object"; // JsonNode 등
    }
}
