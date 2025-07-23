package com.tapioca.MCPBE.controller;

import com.tapioca.MCPBE.service.GptService;
import com.tapioca.MCPBE.util.common.CommonResponseDto;
import com.tapioca.MCPBE.domain.dto.GptRequestDto;
import com.tapioca.MCPBE.domain.dto.GptResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GptController {
    @Autowired
    private final GptService gptService;

    @Value("${gpt.api.model}")
    private String model;

    @PostMapping("/api/gpt-request")
    public CommonResponseDto<?> gptRequest(@RequestBody GptRequestDto requestDto) {
        requestDto.setModel(model);
        GptResponseDto response = gptService.gptRequest(requestDto);
        return CommonResponseDto.ok(response);
    }
}
