package com.tapioca.MCPBE.domain.dto;

import lombok.Getter;

@Getter
public class GptResponseDto {
    private final String response;

    public GptResponseDto(String response) {
        this.response = response;
    }
}