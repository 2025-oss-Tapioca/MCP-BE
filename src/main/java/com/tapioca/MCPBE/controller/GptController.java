package com.tapioca.MCPBE.controller;

import com.tapioca.MCPBE.service.GptService;
import com.tapioca.MCPBE.util.common.CommonResponseDto;
import com.tapioca.MCPBE.util.gpt.GptRequestDto;
import com.tapioca.MCPBE.util.gpt.GptResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GptController {
    private final GptService gptService;

    @PostMapping("/api/gpt-request")
    public CommonResponseDto<?> gptRequest(@RequestBody GptRequestDto requestDto) {
        GptResponseDto response = gptService.gptRequest(requestDto);
        return CommonResponseDto.ok(response);
    }
}
