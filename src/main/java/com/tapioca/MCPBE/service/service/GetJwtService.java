package com.tapioca.MCPBE.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.service.usecase.GetJwtUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
@RequiredArgsConstructor
public class GetJwtService implements GetJwtUseCase {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getJwtFromLogin(String loginId, String password,String loginPath){
        try {
            // 1. 요청 본문 구성
            String requestBody = String.format("{\"loginId\":\"%s\", \"password\":\"%s\"}", loginId, password);
            System.out.println(requestBody);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // 2. POST 요청 보내기
            ResponseEntity<String> response = restTemplate.exchange(
                    loginPath,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            System.out.println(response);

            // 3. 응답에서 accessToken 파싱
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.get("data").get("accessToken").asText(); // ← 키 이름이 "accessToken"일 경우
            } else {
                throw new RuntimeException("❌ 로그인 실패: HTTP 상태 " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ 로그인 중 예외 발생", e);
        }
    }
}
