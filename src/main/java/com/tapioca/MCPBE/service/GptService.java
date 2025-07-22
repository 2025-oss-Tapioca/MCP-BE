package com.tapioca.MCPBE.service;

import com.tapioca.MCPBE.repository.GptRepository;
import com.tapioca.MCPBE.util.gpt.GptRequestDto;
import com.tapioca.MCPBE.util.gpt.GptResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GptService {

    private final GptRepository gptRepository;

    public GptResponseDto gptRequest(GptRequestDto requestDto) {
        return gptRepository.gptRequest(requestDto);
    }
}
