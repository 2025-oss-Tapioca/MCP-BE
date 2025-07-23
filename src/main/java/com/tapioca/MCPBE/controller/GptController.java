package com.tapioca.MCPBE.controller;

import com.tapioca.MCPBE.domain.dto.gpt.BeRequestDto;
import com.tapioca.MCPBE.service.GptService;
import com.tapioca.MCPBE.util.common.CommonResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GptController {

    private final GptService gptService;

    @PostMapping("/api/gpt-request")
    public CommonResponseDto<?> gptRequest(@RequestBody BeRequestDto beRequestDto) {
        return CommonResponseDto.ok(gptService.gptRequest(beRequestDto));
    }
}
