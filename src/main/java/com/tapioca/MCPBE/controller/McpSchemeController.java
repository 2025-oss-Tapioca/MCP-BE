package com.tapioca.MCPBE.controller;

import com.tapioca.MCPBE.domain.dto.request.VegetaTestRequestDto;
import com.tapioca.MCPBE.service.usecase.McpSchemaUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaCommonUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class McpSchemeController {

    private final McpSchemaUseCase mcpSchemaUseCase;
    private final VegetaCommonUseCase vegetaCommonUseCase;

    @GetMapping("/mcp/schema")
    public Map<String, Object> schema() {
        return mcpSchemaUseCase.buildSchema();
    }

    @GetMapping("/sonny")
    public String runVegeta(
            @RequestBody VegetaTestRequestDto vegetaTestRequestDto
    ){
        return vegetaCommonUseCase.execute(
                vegetaTestRequestDto.method(),
                vegetaTestRequestDto.url(),
                vegetaTestRequestDto.loginPath(),
                vegetaTestRequestDto.loginId(),
                vegetaTestRequestDto.password(),
                vegetaTestRequestDto.rate(),
                vegetaTestRequestDto.duration(),
                vegetaTestRequestDto.jsonBody()
        );
    }

}
