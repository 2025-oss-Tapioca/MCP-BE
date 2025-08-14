package com.tapioca.MCPBE.domain.dto.request;

public record LoginRequestDto(
        String loginId, String password,String loginPath
) {
}
