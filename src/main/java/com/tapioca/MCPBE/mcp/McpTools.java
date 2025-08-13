package com.tapioca.MCPBE.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.domain.dto.result.CodeSkeletonResultDto;
import com.tapioca.MCPBE.domain.dto.result.GitPushResultDto;
import com.tapioca.MCPBE.domain.dto.result.SpecTestResultDto;
import com.tapioca.MCPBE.domain.dto.result.TrafficTestResultDto;
import com.tapioca.MCPBE.service.service.CodeSkeletonService;
import com.tapioca.MCPBE.service.service.GitPushService;
import com.tapioca.MCPBE.service.service.trafficAndSpec.SpecTestService;
import com.tapioca.MCPBE.service.service.trafficAndSpec.TrafficTestService;
import com.tapioca.MCPBE.service.usecase.CodeSkeletonUseCase;
import com.tapioca.MCPBE.service.usecase.GitPushUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.SpecTestUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.TrafficTestUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class McpTools {

    private final TrafficTestUseCase trafficTestUseCase;
    private final SpecTestUseCase specTestUseCase;
    private final CodeSkeletonUseCase codeSkeletonUseCase;
    private final GitPushUseCase gitPushUseCase;

    @Tool(
            name = "traffic_test",
            description = "API 트래픽 테스트를 실행합니다"
    )
    public TrafficTestResultDto getTrafficTest(
            @ToolParam(description = "요청 HTTP 메서드 타입을 지정합니다. 예: GET, POST, PUT 등") String method,
            @ToolParam(description = "성능 테스트를 실행할 API의 전체 URL입니다") String url,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 loginPath입니다") String loginPath,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 사용자 로그인 ID입니다") String loginId,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 사용자 비밀번호입니다") String password,
            @ToolParam(description = "초당 전송할 요청 수를 지정합니다 (요청 속도)") int rate,
            @ToolParam(description = "테스트를 수행할 전체 시간(초)을 지정합니다") int duration,
            @ToolParam(description = "API 요청 시 함께 보낼 JSON 본문 데이터입니다") JsonNode jsonBody
    ) {
        return trafficTestUseCase.execute(method, url, loginPath, loginId, password, rate, duration, jsonBody);
    }

    @Tool(
            name = "spec_test",
            description = "API 스펙 테스트를 실행합니다"
    )
    public SpecTestResultDto getSpecTest(
            @ToolParam(description = "요청 HTTP 메서드 타입을 지정합니다. 예: GET, POST, PUT 등") String method,
            @ToolParam(description = "성능 테스트를 실행할 API의 전체 URL입니다") String url,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 loginPath입니다") String loginPath,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 사용자 로그인 ID입니다") String loginId,
            @ToolParam(description = "Jwt 토큰 호출에 사용할 사용자 비밀번호입니다") String password,
            @ToolParam(description = "초당 전송할 요청 수를 지정합니다 (요청 속도)") int rate,
            @ToolParam(description = "테스트를 수행할 전체 시간(초)을 지정합니다") int duration,
            @ToolParam(description = "API 요청 시 함께 보낼 JSON 본문 데이터입니다") JsonNode jsonBody
    ) {
        return specTestUseCase.execute(method, url, loginPath, loginId, password, rate, duration, jsonBody);
    }

    @Tool(
            name = "code_skeleton",
            description = "코드 스캐폴딩에 필요한 TEAM ERD 정보를 가져옵니다."
    )
    public CodeSkeletonResultDto codeSkeleton(
            @ToolParam(description = "팀 erd를 가져오는데 필요한 teamCode입니다.") String teamCode
    ) {
        return codeSkeletonUseCase.execute(teamCode);
    }

    @Tool(
            name = "git_push",
            description = "깃 푸시에 필요한 Github Access Token을 가져옵니다."
    )
    public GitPushResultDto gitPush(
            @ToolParam(description = "Github Access Token을 가져오는데 필요한 teamCode입니다.") String teamCode
    ) {
        return gitPushUseCase.execute(teamCode);
    }
}
