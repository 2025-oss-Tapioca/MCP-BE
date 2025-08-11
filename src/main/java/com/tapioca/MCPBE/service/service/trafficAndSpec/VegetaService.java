package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaUseCase;
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
public class VegetaService implements VegetaUseCase {

    private final String vegetaPath = "C:\\Users\\정유현\\go\\bin\\vegeta.exe";

    @Override
    public String makeTargetFile(String method, String url, String jwt, JsonNode apiDto) throws IOException {
        Path targetFile = Files.createTempFile("vegeta-targets", ".txt");

        String targetContent = String.format(
                "%s %s%nAuthorization: Bearer %s%nContent-Type: application/json%n%n%s",
                method.toUpperCase(),
                url,
                jwt,
                apiDto.toString()
        );

        Files.writeString(targetFile, targetContent, StandardCharsets.UTF_8);
        return targetFile.toAbsolutePath().toString();
    }

    @Override
    public String runVegeta(String targetPath, int rate, int duration) {
        try {
            String cmd = String.format("\"%s\" attack -rate=%d -duration=%ds -targets \"%s\" | \"%s\" report",
                    vegetaPath, rate, duration, targetPath, vegetaPath);

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

            return output.toString();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // InterruptedException 발생 시 인터럽트 복구
            throw new RuntimeException("❌ Vegeta 실행 중 예외 발생", e);
        }
    }
}
