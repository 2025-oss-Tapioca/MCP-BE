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
import com.tapioca.MCPBE.service.usecase.LogContextByLevelUseCase;
import com.tapioca.MCPBE.service.usecase.LogRangeQueryUseCase;
import com.tapioca.MCPBE.domain.dto.request.LogLineDto;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Component
@RequiredArgsConstructor
public class McpTools {

    private final TrafficTestUseCase trafficTestUseCase;
    private final SpecTestUseCase specTestUseCase;
    private final CodeSkeletonUseCase codeSkeletonUseCase;
    private final GitPushUseCase gitPushUseCase;
    private final LogRangeQueryUseCase logRangeQueryUseCase;
    private final LogRangeQueryUseCase logContextByLevelUseCase;

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

    @Tool(
            name = "log_range",
            description = "팀/소스 로그를 기간으로 조회합니다. 날짜는 ISO('2025-08-19T15:00'), 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd HH:mm', epoch(sec/ms) 등 유연 포맷 지원."
    )
    public List<LogLineDto> logRange(
            @ToolParam(description = "BACKEND | FRONTEND | RDS") String sourceType,
            @ToolParam(description = "팀 코드") String teamCode,
            @ToolParam(description = "from 시각") String from,
            @ToolParam(description = "to 시각") String to
    ) {
        return logRangeQueryUseCase.query(sourceType, teamCode, from, to);
    }

    @Tool(
            name = "log_context_by_level",
            description = "특정 레벨의 첫 매칭 로그 이전 N줄 컨텍스트를 포함해 반환합니다. context 기본 50, 최대 500."
    )
    public List<LogLineDto> logContextByLevel(
            @ToolParam(description = "BACKEND | FRONTEND | RDS") String sourceType,
            @ToolParam(description = "팀 코드") String teamCode,
            @ToolParam(description = "로그 레벨 (예: ERROR, WARN, INFO)") String level,
            @ToolParam(description = "이전 컨텍스트 줄 수 (옵션, 기본 50)") String context
    ) {
        return logContextByLevelUseCase.query(sourceType, teamCode, level, context);
    }
}
