package com.tapioca.MCPBE.controller;

import com.tapioca.MCPBE.domain.dto.be.BeRequestDto;
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
    public CommonResponseDto<?> gptRequest(@RequestBody BeRequestDto beRequestDto) {
        beUseCase.beRequest(beRequestDto);
        return CommonResponseDto.ok(null);
    }
}   
