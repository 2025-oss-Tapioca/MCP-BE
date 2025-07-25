package com.tapioca.MCPBE.service.usecase;

import com.tapioca.MCPBE.domain.dto.be.BeRequestDto;
import com.tapioca.MCPBE.domain.dto.gpt.GptResponseDto;

public interface BeUseCase {
    public void beRequest(BeRequestDto beRequestDto);
}
