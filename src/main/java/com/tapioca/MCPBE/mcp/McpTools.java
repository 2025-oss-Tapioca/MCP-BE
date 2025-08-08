package com.tapioca.MCPBE.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tapioca.MCPBE.domain.dto.result.SpecTestResultDto;
import com.tapioca.MCPBE.domain.dto.result.TrafficTestResultDto;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class McpTools {
    @Tool(
            name = "traffic_test",
            description = "api 트래픽 테스트를 진행합니다"
    )
    public TrafficTestResultDto getTrafficTest(
            @ToolParam(description = "요청 HTTP 메서드 타입을 지정합니다. 예: GET, POST, PUT 등") String method,
            @ToolParam(description = "성능 테스트를 실행할 API의 전체 URL입니다") String url,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 사용자 로그인 ID입니다") String loginId,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 사용자 비밀번호입니다") String password,
            @ToolParam(description = "초당 전송할 요청 수를 지정합니다 (요청 속도)") String rate,
            @ToolParam(description = "테스트를 수행할 전체 시간(초)을 지정합니다") String duration,
            @ToolParam(description = "API 요청 시 함께 보낼 JSON 본문 데이터입니다") JsonNode jsonBody
    ){
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode requestsNode = mapper.createObjectNode();
        requestsNode.put("total", 3);
        requestsNode.put("rate", 1.5);
        requestsNode.put("throughput", 1.47);

        ObjectNode inNode = mapper.createObjectNode();
        inNode.put("total", 435);
        inNode.put("mean", 145.0);

        ObjectNode outNode = mapper.createObjectNode();
        outNode.put("total", 0);
        outNode.put("mean", 0.0);

        ObjectNode bytesNode = mapper.createObjectNode();
        bytesNode.set("in", inNode);
        bytesNode.set("out", outNode);

        return new TrafficTestResultDto(requestsNode, bytesNode);
    }

    @Tool(
            name = "spec_test",
            description = "api 성능 테스트를 진행합니다"
    )
    public SpecTestResultDto getSpecTest(
            @ToolParam(description = "요청 HTTP 메서드 타입을 지정합니다. 예: GET, POST, PUT 등") String method,
            @ToolParam(description = "성능 테스트를 실행할 API의 전체 URL입니다") String url,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 사용자 로그인 ID입니다") String loginId,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 사용자 비밀번호입니다") String password,
            @ToolParam(description = "초당 전송할 요청 수를 지정합니다 (요청 속도)") String rate,
            @ToolParam(description = "테스트를 수행할 전체 시간(초)을 지정합니다") String duration,
            @ToolParam(description = "API 요청 시 함께 보낼 JSON 본문 데이터입니다") JsonNode jsonBody
    ){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode latenciesNode = mapper.createObjectNode();
        latenciesNode.put("mean","72.318033ms");
        latenciesNode.put("p50","46.5672ms");
        latenciesNode.put("p95","133.0699ms");
        latenciesNode.put("p99","133.0699ms");
        latenciesNode.put("max","133.0699ms");


        ObjectNode durationNode = mapper.createObjectNode();
        durationNode.put("total","2.0371554s");
        durationNode.put("attack","1.9998384s");
        durationNode.put("waitTime","37.317ms");

        ObjectNode statusCodesNode = mapper.createObjectNode();
        statusCodesNode.put("200" , 3);

        return new SpecTestResultDto(
                latenciesNode,                // JsonNode latencies
                durationNode,                 // JsonNode duration
                1.47,                         // double throughput
                "100.00%",                    // String successRatio
                statusCodesNode               // JsonNode statusCodes
        );
    }
}
