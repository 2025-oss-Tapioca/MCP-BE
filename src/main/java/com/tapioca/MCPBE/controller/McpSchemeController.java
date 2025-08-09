package com.tapioca.MCPBE.controller;

import com.tapioca.MCPBE.service.usecase.McpSchemaUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class McpSchemeController {

    private final McpSchemaUseCase mcpSchemaUseCase;

    @GetMapping("/mcp/schema")
    public Map<String, Object> schema() {
        return mcpSchemaUseCase.buildSchema();
    }

    @GetMapping("/sse")
    public SseEmitter handleSse(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);

        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        SseEmitter emitter = new SseEmitter();
        try {
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/mcp/messages?sessionId=" + session.getId()));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

}
