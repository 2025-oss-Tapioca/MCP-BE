package com.tapioca.MCPBE.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.domain.dto.response.TrafficTestResponseDto;
import com.tapioca.MCPBE.service.usecase.GetJwtUseCase;
import com.tapioca.MCPBE.service.usecase.TrafficTestUseCase;
import com.tapioca.MCPBE.util.parser.TrafficTestParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Transactional
@RequiredArgsConstructor
public class TrafficTestService implements TrafficTestUseCase {

    private final GetJwtUseCase getJwtUseCase;

    @Override
    public TrafficTestResponseDto execute(JsonNode json) {
        String url = json.get("url").asText();
        int rate = json.get("rate").asInt();
        int duration = json.get("duration").asInt();
        String loginId = json.get("login_id").asText();
        String password = json.get("password").asText();
        String loginPath = json.get("login_path").asText();
        String jwt;

        if (loginPath == null){
            jwt=null;
        } else {
            jwt= getJwtUseCase.getJwtFromLogin(loginId,password,loginPath);
        }

        String vegetaPath = "C:\\Users\\정유현\\go\\bin\\vegeta.exe";

        try {
            // 1. 타겟 파일 생성
            Path targetFile = Files.createTempFile("vegeta-targets", ".txt");
            String targetContent = String.format("GET %s%nAuthorization: %s", url, jwt);
            Files.writeString(targetFile, targetContent, StandardCharsets.UTF_8);

            // 2. 전체 명령어 구성
            String cmd = String.format("\"%s\" attack -rate=%d -duration=%ds -targets \"%s\" | \"%s\" report",
                    vegetaPath, rate, duration, targetFile.toAbsolutePath(), vegetaPath);

            // 3. ProcessBuilder 구성
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmd);
            builder.redirectErrorStream(true);

            // 4. 환경변수에 vegeta 경로 추가
            builder.environment().merge("PATH", ";C:\\Users\\정유현\\go\\bin", (oldVal, newVal) -> oldVal + newVal);

            // 5. 명령어 실행 및 출력 수집
            Process process = builder.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("❌ Vegeta 실행 실패 - exit code: " + exitCode + "\n" + output);
            }

            return TrafficTestParser.parse(output.toString());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // InterruptedException 발생 시 인터럽트 복구
            throw new RuntimeException("❌ Vegeta 실행 중 예외 발생", e);
        }
    }


}
