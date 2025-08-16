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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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

    @Value("${loadtest.vegeta.httpTimeoutSec:5}")
    private int httpTimeoutSec; // Vegeta per-request HTTP timeout (-timeout)

    @Value("${loadtest.vegeta.attackExtraWaitSec:120}")
    private int attackExtraWaitSec; // duration 외 여유 시간

    @Value("${loadtest.vegeta.reportMaxWaitSec:300}")
    private int reportMaxWaitSec; // report 단계 최대 대기

    @Value("${loadtest.vegeta.maxWorkers:}")
    private String maxWorkers;

    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String makeTargetFile(String method, String url, String jwt, JsonNode body) throws IOException {
        String upperMethod = (method == null ? "GET" : method.trim().toUpperCase(Locale.ROOT));
        if (!Set.of("GET","POST","PUT","PATCH","DELETE","HEAD","OPTIONS").contains(upperMethod)) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL required");
        }

        url = url.trim().replace("\r","").replace("\n","");
        if (jwt != null) jwt = jwt.replace("\r","").replace("\n","");

        boolean hasBody = (body != null && !body.isEmpty());
        boolean sendBody = hasBody && METHODS_WITH_BODY.contains(upperMethod);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("method", upperMethod);
        root.put("url", url);

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
            String bodyJson = objectMapper.writeValueAsString(body);
            String base64 = Base64.getEncoder().encodeToString(bodyJson.getBytes(StandardCharsets.UTF_8));
            root.put("body", base64);
        }

        String line = objectMapper.writeValueAsString(root) + "\n";

        Path path = Paths.get(targetFilePath).toAbsolutePath();
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, line.getBytes(StandardCharsets.UTF_8));

        return path.toString();
    }

    public String runVegeta(String targetPath, int rate, int durationSec) {
        final String bin = resolveVegetaBin();
        Path outBin = null;
        try {
            outBin = Files.createTempFile("vegeta-", ".bin");

            List<String> args = new ArrayList<>();
            args.add(bin); args.add("attack");
            args.add("-rate"); args.add(String.valueOf(rate));
            args.add("-duration"); args.add(durationSec + "s");
            args.add("-targets"); args.add(Paths.get(targetPath).toAbsolutePath().toString());
            args.add("-format"); args.add("json");
            args.add("-timeout"); args.add(Math.max(1, httpTimeoutSec) + "s");
            if (maxWorkers != null && !maxWorkers.isBlank()) {
                args.add("-max-workers"); args.add(maxWorkers.trim());
            }

            ProcessBuilder attackPb = new ProcessBuilder(args);
            attackPb.redirectOutput(outBin.toFile());
            attackPb.redirectErrorStream(false);

            Process attack = attackPb.start();

            StringBuilder errBuf = new StringBuilder();
            Thread errGobbler = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(attack.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        errBuf.append(line).append('\n');
                    }
                } catch (IOException ignored) {}
            });
            errGobbler.setDaemon(true);
            errGobbler.start();

            int attackWait = Math.max(durationSec + 120, durationSec * 2 + 60);
            boolean finished = attack.waitFor(attackWait, TimeUnit.SECONDS);

            if (!finished) {
                attack.destroyForcibly();
                throw new RuntimeException("vegeta attack timeout (wait=" + attackWait + "s)");
            }

            errGobbler.join(3000);

            if (attack.exitValue() != 0) {
                throw new RuntimeException(
                        "vegeta attack failed (exit=" + attack.exitValue() + ")\n" +
                                "stderr:\n" + errBuf
                );
            }

            Process report = new ProcessBuilder(
                    bin, "report",
                    "-type", "json",
                    outBin.toString()
            ).redirectErrorStream(true).start();

            boolean reportDone = report.waitFor(Math.max(60, Math.min(reportMaxWaitSec, attackWait)), TimeUnit.SECONDS);
            String json = new String(report.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if (!reportDone || report.exitValue() != 0) {
                throw new RuntimeException("vegeta report failed (exit=" + report.exitValue() + ", done=" + reportDone + ")");
            }

            return json;

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new RuntimeException("Vegeta 실행 실패 - vegeta 경로 또는 실행 환경 확인", e);
        } finally {
            try {
                if (outBin != null) Files.deleteIfExists(outBin);
            } catch (IOException ignore) {}
        }
    }

    private String resolveVegetaBin() {
        return vegetaBin.trim();
    }
}
