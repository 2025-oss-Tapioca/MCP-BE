package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class VegetaService implements VegetaUseCase {

    @Value("${loadtest.vegeta.bin:}")
    private String vegetaBin; // application.yml에서 지정된 vegeta 경로 사용

    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");

    public String makeTargetFile(String method, String url, String jwt, JsonNode body) throws IOException {
        final String m = (method == null ? "GET" : method.trim().toUpperCase());
        final boolean hasJwt  = jwt != null && !jwt.isBlank();
        final boolean hasBody = body != null && !body.isNull() && METHODS_WITH_BODY.contains(m);

        System.out.println("[vegeta] method=" + m + " url=" + url + " hasBody=" + hasBody);

        StringBuilder sb = new StringBuilder();
        sb.append(m).append(" ").append(url).append("\n");
        if (hasJwt) sb.append("Authorization: Bearer ").append(jwt).append("\n");

        if (hasBody) {
            sb.append("Content-Type: application/json").append("\n\n");
            // JSON 안정적으로 직렬화
            String jsonBodyString = new ObjectMapper().writeValueAsString(body);
            sb.append(jsonBodyString).append("\n");
        } else {
            sb.append("\n");
        }

        String finalContent = sb.toString().replace("\r\n", "\n");

        Path target = Files.createTempFile("vegeta-targets", ".txt");
        Files.writeString(target, finalContent, StandardCharsets.UTF_8);

        System.out.println("=== Vegeta Target File ===\n" + finalContent);

        return target.toAbsolutePath().toString();
    }

    public String runVegeta(String targetPath, int rate, int durationSec) {
        final String bin = resolveVegetaBin();
        try {
            Path outBin = Files.createTempFile("vegeta-", ".bin");
            // 실제 생성되는 파일명 예: /tmp/vegeta-1234567890.bin (임시 디렉토리에 랜덤 숫자 포함)

            ProcessBuilder attackPb = new ProcessBuilder(
                    bin, "attack",
                    "-rate", String.valueOf(rate),
                    "-duration", durationSec + "s",
                    "-targets", targetPath
            );
            attackPb.redirectOutput(outBin.toFile());
            attackPb.redirectErrorStream(false);

            Process attack = attackPb.start();

            // stderr 비동기 수집
            StringBuilder errBuf = new StringBuilder();
            Thread errGobbler = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(attack.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) errBuf.append(line).append('\n');
                } catch (IOException ignore) {}
            });
            errGobbler.setDaemon(true);
            errGobbler.start();

            boolean finished = attack.waitFor(durationSec + 30L, TimeUnit.SECONDS);
            if (!finished) {
                attack.destroyForcibly();
                throw new RuntimeException("vegeta attack timeout");
            }
            try { errGobbler.join(1500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

            if (attack.exitValue() != 0) {
                String targetsPreview = Files.readString(Path.of(targetPath), StandardCharsets.UTF_8);
                throw new RuntimeException(
                        "vegeta attack failed (exit=" + attack.exitValue() + ")\n" +
                                "stderr:\n" + errBuf + "\n" +
                                "targets:\n" + targetsPreview
                );
            }

            Process report = new ProcessBuilder(
                    bin, "report",
                    "-type", "json",
                    outBin.toString()
            ).redirectErrorStream(true).start();

            String json = new String(report.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            report.waitFor(15, TimeUnit.SECONDS);
            if (report.exitValue() != 0) {
                throw new RuntimeException("vegeta report failed (exit=" + report.exitValue() + ")");
            }

            return json;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new RuntimeException("Vegeta 실행 실패 - vegeta 경로 또는 실행 환경 확인", e);
        }
    }

    /**
     * 경로 체크 없이 설정된 vegetaBin 그대로 사용
     */
    private String resolveVegetaBin() {
        return vegetaBin.trim();
    }
}
