package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
            sb.append(body.toString()).append("\n");
        } else {
            sb.append("\n");
        }

        Path target = Files.createTempFile("vegeta-targets", ".txt");
        Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);

        return target.toAbsolutePath().toString();
    }

    public String runVegeta(String targetPath, int rate, int durationSec) {
        System.out.println("runVegeta 실행");
        final String bin = resolveVegetaBin();
        try {
            Path outBin = Files.createTempFile("vegeta-", ".bin");

            ProcessBuilder attackPb = new ProcessBuilder(
                    bin, "attack",
                    "-rate", String.valueOf(rate),
                    "-duration", durationSec + "s",
                    "-targets", targetPath
            );
            // stdout -> 파일, stderr -> 별도 캡처
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
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new RuntimeException("Vegeta 실행 실패 - vegeta 경로 또는 실행 환경 확인", e);
        }
    }

    /**
     * 지정된 vegetaBin 경로만 사용
     */
    private String resolveVegetaBin() {
        if (vegetaBin == null || vegetaBin.isBlank()) {
            throw new IllegalStateException("vegeta 실행 파일 경로가 설정되지 않았습니다. application.yml에서 loadtest.vegeta.bin을 지정하세요.");
        }
        Path p = Path.of(vegetaBin.trim());
        if (!Files.isExecutable(p)) {
            throw new IllegalStateException("지정된 vegeta 실행 파일이 존재하지 않거나 실행 권한이 없습니다: " + vegetaBin);
        }
        return p.toString();
    }
}


