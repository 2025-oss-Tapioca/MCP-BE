// com.tapioca.MCPBE.service.service.log.BeLogContextByLevelService
package com.tapioca.MCPBE.service.service;

import com.tapioca.MCPBE.domain.dto.request.LogLineDto;
import com.tapioca.MCPBE.service.usecase.LogContextByLevelUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogContextByLevelService implements LogContextByLevelUseCase {

    private final @Qualifier("beWebClient") WebClient beWebClient;

    @Override
    public List<LogLineDto> query(String sourceType, String teamCode, String level, Integer context) {
        int ctx = (context == null) ? 50 : Math.max(0, Math.min(context, 500));
        return beWebClient.get()
                .uri(uri -> uri.path("/api/log/query/context")
                        .queryParam("sourceType", sourceType)
                        .queryParam("teamCode", teamCode)
                        .queryParam("level", level)
                        .queryParam("context", ctx)
                        .build())
                .retrieve()
                .onStatus(s -> s.isError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(mapBeError(b))))
                .bodyToFlux(LogLineDto.class)
                .collectList()
                .block();
    }

    private RuntimeException mapBeError(String body) {
        if (body != null && (body.contains("\"40710\"") || body.contains("LOG_COLLECTION_REQUIRED"))) {
            return new RuntimeException("로그 수집이 시작되지 않았습니다. 먼저 '로그 수집 시작' 버튼을 누르고 다시 시도하세요.");
        }
        return new RuntimeException("BE context query error: " + body);
    }
}
