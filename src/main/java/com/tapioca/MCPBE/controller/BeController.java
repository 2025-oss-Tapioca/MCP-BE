package com.tapioca.MCPBE.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.service.usecase.BeUseCase;
import com.tapioca.MCPBE.util.common.CommonResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BeController {

    private final BeUseCase beUseCase;

    @PostMapping("/api/gpt-request")
    public CommonResponseDto<?> gptRequest(@RequestBody JsonNode beJson) {
        return CommonResponseDto.ok(beUseCase.beRequest(beJson));
    }
}
