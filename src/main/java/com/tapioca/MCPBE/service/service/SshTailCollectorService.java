package com.tapioca.MCPBE.service.service;

import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.LogCollectorUseCase;
import com.tapioca.MCPBE.util.common.LogLineParser;
import com.tapioca.MCPBE.util.websocket.BeWsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier; // TODO: 운영은 KnownHosts 권장
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshTailCollectorService implements LogCollectorUseCase {

    private final BeWsClient ws;
    private final LogLineParser parser = new LogLineParser();
    private final LogCollectionSupervisor supervisor;   // ✅ 30분 자동 종료 스케줄러

    private final ExecutorService exec = Executors.newCachedThreadPool();
    private final ConcurrentMap<String, Future<?>> running = new ConcurrentHashMap<>();

    @Override
    public void start(String internalKey, String type, String teamCode, String callbackUrl, Map<String, Object> cfg) {
        running.computeIfAbsent(internalKey, k -> {
            Future<?> f = exec.submit(() -> runTailLoop(internalKey, type, teamCode, callbackUrl, cfg));
            // 시작과 동시에 기본 TTL(설정값, 기본 30분) 타임아웃 예약
            supervisor.armDefaultTimeout(() -> stop(internalKey), internalKey);
            return f;
        });
    }

    @Override
    public void stop(String internalKey) {
        // 예약된 타임아웃 취소
        supervisor.cancelTimeout(internalKey);
        Future<?> f = running.remove(internalKey);
        if (f != null) f.cancel(true);
    }

    private void runTailLoop(String internalKey, String type, String teamCode,
                             String callbackUrl, Map<String, Object> cfg) {
        final String host    = str(cfg.get("host"));
        final String user    = strOrDefault(cfg.get("user"), "ubuntu");
        final String keyPem  = str(cfg.get("authToken"));          // PEM 문자열
        final String logPath = strOrDefault(cfg.get("logPath"), "/var/log/syslog");

        if (host == null || keyPem == null) {
            log.error("[SSH] missing host/authToken for {}", internalKey);
            throw new CustomException(ErrorCode.INVALID_REQUEST_BODY);
        }

        ws.connectAndRegister(internalKey, callbackUrl, type, teamCode);

        while (!Thread.currentThread().isInterrupted()) {
            Path tmp = null;
            try (SSHClient ssh = new SSHClient()) {
                ssh.addHostKeyVerifier(new PromiscuousVerifier()); // TODO: 운영은 KnownHosts로 교체
                ssh.connect(host, 22);

                // PEM 문자열을 임시 파일로 저장 후 로드 (버전 호환성 최고)
                tmp = Files.createTempFile("mcpbe_key_", ".pem");
                Files.writeString(tmp, keyPem, StandardCharsets.UTF_8);
                tmp.toFile().setReadable(true, true);
                KeyProvider keyProvider = ssh.loadKeys(tmp.toString(), null, null);
                Files.deleteIfExists(tmp); // 로드 후 즉시 삭제

                try {
                    ssh.authPublickey(user, keyProvider);
                } catch (Exception authEx) {
                    log.warn("[SSH] auth failed {}@{}: {}", user, host, authEx.getMessage());
                    throw new CustomException(ErrorCode.LOG_SSH_AUTH_FAILED);
                }

                try (Session session = ssh.startSession()) {
                    String cmd = "tail -F " + shellEscape(logPath);
                    Session.Command command = session.exec(cmd);

                    try (InputStream is = command.getInputStream();
                         BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                            ws.sendLog(internalKey, parser.toMap(line, type, teamCode));
                        }
                    } finally {
                        IOUtils.readFully(command.getErrorStream()).close();
                        command.close();
                    }
                }
            } catch (CustomException ce) {
                if (tmp != null) try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
                throw ce;
            } catch (java.net.ConnectException ce) {
                if (tmp != null) try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
                log.warn("[SSH] connect failed {}@{} ({}). retry in 2s", user, host, ce.getMessage());
                sleep(2000);
                throw new CustomException(ErrorCode.LOG_SSH_CONNECTION_FAILED);
            } catch (java.io.IOException ioe) {
                if (tmp != null) try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
                log.warn("[SSH] I/O error {}: {}  (retry in 2s)", internalKey, ioe.getMessage());
                sleep(2000);
            } catch (Exception e) {
                if (tmp != null) try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
                log.error("[SSH] execution error {}: {}", internalKey, e.toString(), e);
                throw new CustomException(ErrorCode.LOG_SSH_EXECUTION_FAILED);
            }
        }

        log.info("[SSH] stopped {}", internalKey);
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static String strOrDefault(Object o, String d) { return o == null ? d : String.valueOf(o); }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); } }
    private static String shellEscape(String p) { return "'" + p.replace("'", "'\\''") + "'"; }
}
