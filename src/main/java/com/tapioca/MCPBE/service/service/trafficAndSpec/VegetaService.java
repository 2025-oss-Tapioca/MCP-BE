package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class VegetaService implements VegetaUseCase {

    @Value("${loadtest.vegeta.bin}")
    private String vegetaBin; // vegeta 실행 경로

    @Value("${loadtest.vegeta.targetPath}")
    private String targetFilePath; // vegeta 타겟 파일 경로(단일 요청을 덮어쓸 때 사용)

    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Vegeta 타겟 파일(JSON 포맷, NDJSON 1줄) 생성
     */
    @Override
    public String makeTargetFile(String method, String url, String jwt, JsonNode body) throws IOException {
        String upperMethod = (method == null ? "GET" : method.trim().toUpperCase(Locale.ROOT));
        if (!Set.of("GET","POST","PUT","PATCH","DELETE","HEAD","OPTIONS").contains(upperMethod)) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL required");
        }

        // 파싱 오류 방지: 줄바꿈 제거
        url = url.trim().replace("\r","").replace("\n","");
        if (jwt != null) jwt = jwt.replace("\r","").replace("\n","");

        boolean hasBody = (body != null && !body.isEmpty());
        boolean sendBody = hasBody && METHODS_WITH_BODY.contains(upperMethod);

        // ---- Vegeta JSON 타겟 1건 구성 ----
        ObjectNode root = objectMapper.createObjectNode();
        root.put("method", upperMethod);
        root.put("url", url);

        // header: 각 키는 배열이어야 함
        ObjectNode header = objectMapper.createObjectNode();
        if (jwt != null && !jwt.isBlank()) {
            header.putArray("Authorization").add("Bearer " + jwt);
        }
        if (sendBody) {
            header.putArray("Content-Type").add("application/json; charset=UTF-8");
        }
        if (!header.isEmpty()) {
            root.set("header", header);
        }

        if (sendBody) {
            // 바디를 JSON 문자열로 직렬화 → UTF-8 바이트 → Base64
            String bodyJson = objectMapper.writeValueAsString(body);
            String base64 = Base64.getEncoder().encodeToString(bodyJson.getBytes(StandardCharsets.UTF_8));
            root.put("body", base64);
        }

        String line = objectMapper.writeValueAsString(root) + "\n"; // NDJSON: 한 줄에 한 요청

        // 파일 저장 (BOM 없음)
        Path path = Paths.get(targetFilePath).toAbsolutePath();
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, line.getBytes(StandardCharsets.UTF_8));

        return path.toString();
    }

    /**
     * Vegeta 실행(JSON 포맷)
     */
    public String runVegeta(String targetPath, int rate, int durationSec) {
        System.out.println("=== runVegeta(JSON) ===");
        System.out.println("[입력값] targetPath=" + targetPath + ", rate=" + rate + ", durationSec=" + durationSec);

        // 실행 전 파일 확인 (민감정보는 출력 금지/마스킹 권장)
        try {
            Path absoluteTarget = Paths.get(targetPath).toAbsolutePath();
            System.out.println("실제 절대경로: " + absoluteTarget);
            if (Files.exists(absoluteTarget)) {
                String preview = Files.readString(absoluteTarget, StandardCharsets.UTF_8)
                        .replaceAll("(?i)(Authorization\"\\s*:\\s*\\[\\s*\")Bearer [^\"]+\"", "$1Bearer ******\"");
                System.out.println("--- 파일 내용 (masked) 시작 ---");
                System.out.println(preview);
                System.out.println("--- 파일 내용 (masked) 끝 ---");
            } else {
                System.out.println("[경고] 해당 경로에 파일이 존재하지 않습니다!");
            }
        } catch (IOException e) {
            System.out.println("[오류] target 파일 내용 읽기 실패: " + e.getMessage());
        }

        final String bin = resolveVegetaBin();
        System.out.println("[vegeta] 실행 파일 경로: " + bin);

        Path outBin = null;
        try {
            outBin = Files.createTempFile("vegeta-", ".bin");
            System.out.println("[vegeta] 결과 저장 bin 파일 경로: " + outBin);

            ProcessBuilder attackPb = new ProcessBuilder(
                    bin, "attack",
                    "-rate", String.valueOf(rate),
                    "-duration", durationSec + "s",
                    "-targets", Paths.get(targetPath).toAbsolutePath().toString(),
                    "-format", "json" // ★ JSON 포맷 사용 ★
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
                        // Authorization 마스킹
                        line = line.replaceAll("(?i)(Authorization:\\s*Bearer )\\S+", "$1******");
                        System.out.println("[stderr] " + line);
                        errBuf.append(line).append('\n');
                    }
                } catch (IOException e) {
                    System.out.println("[stderr 읽기 오류] " + e.getMessage());
                }
            });
            errGobbler.setDaemon(true);
            errGobbler.start();

            boolean finished = attack.waitFor(Math.max(durationSec + 30, 60), TimeUnit.SECONDS);
            System.out.println("[vegeta] attack 종료 여부: " + finished);

            if (!finished) {
                System.out.println("[오류] vegeta attack timeout");
                attack.destroyForcibly();
                throw new RuntimeException("vegeta attack timeout");
            }

            errGobbler.join(3000);

            if (attack.exitValue() != 0) {
                String targetsPreview = Files.readString(Path.of(targetPath), StandardCharsets.UTF_8)
                        .replaceAll("(?i)(\"Authorization\"\\s*:\\s*\\[\\s*\")Bearer [^\"]+\"", "$1Bearer ******\"");
                System.out.println("[오류] vegeta attack 실패 - exitCode=" + attack.exitValue());
                throw new RuntimeException(
                        "vegeta attack failed (exit=" + attack.exitValue() + ")\n" +
                                "stderr:\n" + errBuf + "\n" +
                                "targets (masked):\n" + targetsPreview
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

            report.waitFor(30, TimeUnit.SECONDS);
            System.out.println("[vegeta] report 종료 코드=" + report.exitValue());

            if (report.exitValue() != 0) {
                System.out.println("[오류] vegeta report 실패");
                throw new RuntimeException("vegeta report failed (exit=" + report.exitValue() + ")");
            }

            return json;

        } catch (IOException | InterruptedException e) {
            System.out.println("[예외 발생] " + e.getMessage());
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new RuntimeException("Vegeta 실행 실패 - vegeta 경로 또는 실행 환경 확인", e);
        } finally {
            try {
                if (outBin != null) Files.deleteIfExists(outBin);
            } catch (IOException ignore) {}
        }
    }

    private String resolveVegetaBin() {
        System.out.println("[resolveVegetaBin] vegetaBin=" + vegetaBin);
        return vegetaBin.trim();
    }
}
