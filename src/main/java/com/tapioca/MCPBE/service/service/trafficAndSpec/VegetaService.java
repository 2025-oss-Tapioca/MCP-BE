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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Vegeta 타겟 파일 생성
     */
    public String makeTargetFile(String method, String url, String jwt, JsonNode body) throws IOException {
        System.out.println("=== makeTargetFile() 진입 ===");
        System.out.println("[입력값] method=" + method + ", url=" + url + ", jwt=" + jwt + ", body=" + body);

        final String m = (method == null ? "GET" : method.trim().toUpperCase());
        final boolean hasJwt = jwt != null && !jwt.isBlank();
        final boolean hasBody = body != null && !body.isNull() && METHODS_WITH_BODY.contains(m);

        System.out.println("[vegeta] 변환된 method=" + m);
        System.out.println("[vegeta] JWT 존재 여부=" + hasJwt);
        System.out.println("[vegeta] Body 존재 여부=" + hasBody);

        StringBuilder sb = new StringBuilder();
        sb.append(m).append(" ").append(url).append("\n");

        if (hasJwt) {
            System.out.println("[vegeta] Authorization 헤더 추가");
            sb.append("Authorization: Bearer ").append(jwt).append("\n");
        }

        if (hasBody) {
            System.out.println("[vegeta] Content-Type 헤더 추가 + Body 추가");
            sb.append("Content-Type: application/json; charset=UTF-8").append("\n\n");

            String jsonBodyString;
            if (body.isTextual()) {
                System.out.println("[vegeta] Body가 Text 형식 → textValue 사용");
                jsonBodyString = body.textValue();
            } else {
                System.out.println("[vegeta] Body가 JSON 형식 → toString 사용");
                jsonBodyString = body.toString();
            }
            sb.append(jsonBodyString).append("\n");
        } else {
            System.out.println("[vegeta] Body 없음");
            sb.append("\n");
        }

        String finalContent = sb.toString()
                .replace("\uFEFF", "") // BOM 제거
                .replaceAll("^[\\r\\n]+", "") // 맨 앞 불필요한 줄바꿈 제거
                .replace("\r\n", "\n");
        Path target = Files.createTempFile("vegeta-targets", ".txt");
        Files.writeString(target, finalContent, StandardCharsets.UTF_8);

        System.out.println("=== Vegeta Target File 생성 완료 ===");
        System.out.println(finalContent);

        return target.toAbsolutePath().toString();
    }

    /**
     * Vegeta 실행
     */
    public String runVegeta(String targetPath, int rate, int durationSec) {
        System.out.println("=== runVegeta() 진입 ===");
        System.out.println("[입력값] targetPath=" + targetPath + ", rate=" + rate + ", durationSec=" + durationSec);

        final String bin = resolveVegetaBin();
        System.out.println("[vegeta] 실행 파일 경로: " + bin);

        try {
            Path outBin = Files.createTempFile("vegeta-", ".bin");
            System.out.println("[vegeta] 결과 저장 bin 파일 경로: " + outBin);

            ProcessBuilder attackPb = new ProcessBuilder(
                    bin, "attack",
                    "-rate", String.valueOf(rate),
                    "-duration", durationSec + "s",
                    "-targets", targetPath
            );
            attackPb.redirectOutput(outBin.toFile());
            attackPb.redirectErrorStream(false);

            System.out.println("[vegeta] attack 프로세스 시작");
            Process attack = attackPb.start();

            StringBuilder errBuf = new StringBuilder();
            Thread errGobbler = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(attack.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println("[stderr] " + line);
                        errBuf.append(line).append('\n');
                    }
                } catch (IOException e) {
                    System.out.println("[stderr 읽기 오류] " + e.getMessage());
                }
            });
            errGobbler.setDaemon(true);
            errGobbler.start();

            boolean finished = attack.waitFor(durationSec + 30L, TimeUnit.SECONDS);
            System.out.println("[vegeta] attack 종료 여부: " + finished);

            if (!finished) {
                System.out.println("[오류] vegeta attack timeout");
                attack.destroyForcibly();
                throw new RuntimeException("vegeta attack timeout");
            }

            errGobbler.join(1500);

            if (attack.exitValue() != 0) {
                String targetsPreview = Files.readString(Path.of(targetPath), StandardCharsets.UTF_8);
                System.out.println("[오류] vegeta attack 실패 - exitCode=" + attack.exitValue());
                throw new RuntimeException(
                        "vegeta attack failed (exit=" + attack.exitValue() + ")\n" +
                                "stderr:\n" + errBuf + "\n" +
                                "targets:\n" + targetsPreview
                );
            }

            System.out.println("[vegeta] report 생성 시작");
            Process report = new ProcessBuilder(
                    bin, "report",
                    "-type", "json",
                    outBin.toString()
            ).redirectErrorStream(true).start();

            String json = new String(report.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("=== Vegeta Report STDOUT ===\n" + json);

            report.waitFor(15, TimeUnit.SECONDS);
            System.out.println("[vegeta] report 종료 코드=" + report.exitValue());

            if (report.exitValue() != 0) {
                System.out.println("[오류] vegeta report 실패");
                throw new RuntimeException("vegeta report failed (exit=" + report.exitValue() + ")");
            }

            System.out.println("=== runVegeta() 완료 ===");
            return json;

        } catch (IOException | InterruptedException e) {
            System.out.println("[예외 발생] " + e.getMessage());
            e.printStackTrace();
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new RuntimeException("Vegeta 실행 실패 - vegeta 경로 또는 실행 환경 확인", e);
        }
    }

    /**
     * 설정된 vegeta 실행 경로 사용
     */
    private String resolveVegetaBin() {
        System.out.println("[resolveVegetaBin] vegetaBin=" + vegetaBin);
        return vegetaBin.trim();
    }
}
