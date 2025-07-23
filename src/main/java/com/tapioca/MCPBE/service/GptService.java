package com.tapioca.MCPBE.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.domain.dto.gpt.BeRequestDto;
import com.tapioca.MCPBE.domain.dto.gpt.GptResponseDto;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.util.prompt.GptPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GptService {

    private final OpenAiChatModel openAiChatModel;

    public GptResponseDto gptRequest(BeRequestDto beRequestDto) {
        String prompt = makePrompt(beRequestDto.userRequest());
        System.out.println(prompt);
        String gptResult = openAiChatModel.call(prompt);
        ObjectMapper mapper = new ObjectMapper();
        try{
            return mapper.readValue(gptResult, GptResponseDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_MAPPING_VALUE);
        }
    }

    public String makePrompt(String userRequest){
        String prompt = GptPrompt.MCP_PROMPT;
        return prompt.replace("${user_request}",userRequest);
    }
}
