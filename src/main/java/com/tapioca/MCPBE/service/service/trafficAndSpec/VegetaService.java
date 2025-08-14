package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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

    @Value("${loadtest.vegeta.bin:vegeta}")
    private String vegetaBin;

    private static final Set<String> METHODS_WITH_BODY =
            Set.of("POST","PUT","PATCH");

    @Override
    public String makeTargetFile(String method, String url, String jwt, JsonNode body) throws IOException {
        final String m = (method == null ? "GET" : method.trim().toUpperCase());
        final boolean hasJwt  = jwt != null && !jwt.isBlank();
        final boolean hasBody = body != null && !body.isNull() && METHODS_WITH_BODY.contains(m);

        // 디버그: 실제 들어온 URL 확인
        System.out.println("[vegeta] method="+m+" url="+url+" hasBody="+hasBody);

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

    @Override
    public String runVegeta(String targetPath, int rate, int durationSec) {
        try {
            Path outBin = Files.createTempFile("vegeta-", ".bin");

            ProcessBuilder attackPb = new ProcessBuilder(
                    vegetaBin, "attack",
                    "-rate", String.valueOf(rate),
                    "-duration", durationSec + "s",
                    "-targets", targetPath
            );
            attackPb.redirectOutput(outBin.toFile());
            attackPb.redirectErrorStream(false);
            Process attack = attackPb.start();

            String attackStderr = readAll(attack.getErrorStream());  // 🔎 에러 내용 캡처
            boolean finished = attack.waitFor(durationSec + 30, TimeUnit.SECONDS);
            if (!finished) {
                attack.destroyForcibly();
                throw new RuntimeException("vegeta attack timeout");
            }
            if (attack.exitValue() != 0) {
                String targetsPreview = Files.readString(Path.of(targetPath), StandardCharsets.UTF_8);
                throw new RuntimeException(
                        "vegeta attack failed (exit=" + attack.exitValue() + ")\n" +
                                "stderr:\n" + attackStderr + "\n" +
                                "targets:\n" + targetsPreview
                );
            }

            Process report = new ProcessBuilder(
                    vegetaBin, "report",
                    "-type", "json",
                    outBin.toString()
            ).redirectErrorStream(true).start();

            String json = new String(report.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            report.waitFor(15, java.util.concurrent.TimeUnit.SECONDS);
            if (report.exitValue() != 0) {
                throw new RuntimeException("vegeta report failed (exit=" + report.exitValue() + ")");
            }
            return json;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            System.out.println(e.getMessage());
            throw new RuntimeException("Vegeta 실행 실패 - vegeta 경로 또는 실행 환경 확인", e);
        }
    }

    private static String readAll(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line; while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }
}

