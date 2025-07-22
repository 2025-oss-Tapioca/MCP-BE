package com.tapioca.MCPBE.repository;

import com.tapioca.MCPBE.util.gpt.GptRequestDto;
import com.tapioca.MCPBE.util.gpt.GptResponseDto;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

public interface GptRepository {
    GptResponseDto gptRequest(GptRequestDto gptRequestDto);
}
