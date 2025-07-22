package com.tapioca.MCPBE.util.gpt;

import lombok.Getter;

@Getter
public class GptRequestDto {
    private final String prompt;

    public GptRequestDto(String prompt) {
        this.prompt = prompt;
    }
}
