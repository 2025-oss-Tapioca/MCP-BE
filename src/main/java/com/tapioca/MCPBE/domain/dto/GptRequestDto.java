package com.tapioca.MCPBE.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GptRequestDto {
    private String model;
    private List<Message> messages;

    // 내부 Message 클래스 예시
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
