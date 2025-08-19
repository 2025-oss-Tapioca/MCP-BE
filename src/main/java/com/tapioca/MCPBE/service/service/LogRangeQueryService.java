package com.tapioca.MCPBE.service.service;

import com.tapioca.MCPBE.domain.dto.request.LogLineDto;
import com.tapioca.MCPBE.service.usecase.LogRangeQueryUseCase;
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
public class LogRangeQueryService implements LogRangeQueryUseCase {

    private final @Qualifier("beWebClient") WebClient beWebClient;

    @Override
    public List<LogLineDto> query(String sourceType, String teamCode, String from, String to) {
        return beWebClient.get()
                .uri(uri -> uri.path("/api/log/query/range")
                        .queryParam("sourceType", sourceType)
                        .queryParam("teamCode", teamCode)
                        .queryParam("from", from)
                        .queryParam("to", to)
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
        return new RuntimeException("BE range query error: " + body);
    }
}
