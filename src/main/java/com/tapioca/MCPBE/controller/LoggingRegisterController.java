package com.tapioca.MCPBE.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.domain.dto.result.McpLogRegisterRequestDto;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.service.CloudWatchCollectorService;
import com.tapioca.MCPBE.service.service.SshTailCollectorService;
import com.tapioca.MCPBE.util.common.HmacVerifier;
import com.tapioca.MCPBE.util.websocket.BeWsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mcp/logging")
@RequiredArgsConstructor
public class LoggingRegisterController {

    private final ObjectMapper om = new ObjectMapper();
    private final HmacVerifier hmac;
    private final BeWsClient beWs;
    private final SshTailCollectorService sshCollector;
    private final CloudWatchCollectorService cwCollector;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestHeader("X-Signature") String signature,
                                         @RequestBody byte[] raw) throws Exception {
        if (!hmac.verify(raw, signature)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED_ERROR);
        }

        McpLogRegisterRequestDto dto = om.readValue(raw, McpLogRegisterRequestDto.class);

        // BE WS connect + register
        beWs.connectAndRegister(dto.internalKey(), dto.callbackUrl(), dto.type(), dto.teamCode());

        // 수집 시작
        switch (dto.type()) {
            case "BACKEND":
            case "FRONTEND":
                sshCollector.start(dto.internalKey(), dto.type(), dto.teamCode(), dto.callbackUrl(), dto.cfg());
                break;
            case "RDS":
                cwCollector.start(dto.internalKey(), dto.type(), dto.teamCode(), dto.callbackUrl(), dto.cfg());
                break;
            default:
                throw new CustomException(ErrorCode.NOT_FOUND_TYPE);
        }

        return ResponseEntity.accepted().build();
    }
}
