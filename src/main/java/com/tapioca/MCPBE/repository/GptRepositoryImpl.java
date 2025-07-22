package com.tapioca.MCPBE.repository;

import com.tapioca.MCPBE.util.gpt.GptRequestDto;
import com.tapioca.MCPBE.util.gpt.GptResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class GptRepositoryImpl implements GptRepository {
    @Value("${gpt.api-key}")
    private String apiKey;

    @Value("${gpt.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public GptResponseDto gptRequest(GptRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> body = new HashMap<>();
        body.put("model",    "gpt-3.5-turbo");
        body.put("messages", List.of(
                Map.of("role","user","content", requestDto.getPrompt())
        ));

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body, headers);
        @SuppressWarnings("unchecked")
        Map<String,Object> apiResponse = restTemplate
                .postForObject(apiUrl, entity, Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> choices = (List<Map<String,Object>>) apiResponse.get("choices");
        String content = (String)((Map<String,Object>)choices.get(0).get("message")).get("content");

        return new GptResponseDto(content.trim());
    }
}
