package com.tapioca.MCPBE.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogCollectionSupervisor {

    /** 기본 TTL (분) — yml에서 override 가능 */
    @Value("${logging.collector-default-ttl-minutes:30}")
    private long defaultTtlMinutes;

    /** internalKey → 예약된 stop 작업 */
    private final Map<String, ScheduledFuture<?>> timeouts = new ConcurrentHashMap<>();

    /** 타임아웃 스케줄용 */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 수집 시작 직후 호출: 기본 TTL(설정값)로 종료 예약
     */
    public void armDefaultTimeout(Runnable stopAction, String internalKey) {
        armTimeout(stopAction, internalKey, Duration.ofMinutes(defaultTtlMinutes));
    }

    /**
     * 수집 시작 직후 호출: 지정 TTL로 종료 예약
     */
    public void armTimeout(Runnable stopAction, String internalKey, Duration ttl) {
        cancelTimeout(internalKey); // 중복 예약 방지
        ScheduledFuture<?> f = scheduler.schedule(() -> {
            try {
                log.info("[Supervisor] timeout reached for {}, stopping collector", internalKey);
                stopAction.run();
            } catch (Exception e) {
                log.warn("[Supervisor] stop failed for {}: {}", internalKey, e.getMessage(), e);
            } finally {
                timeouts.remove(internalKey);
            }
        }, Math.max(1, ttl.toSeconds()), TimeUnit.SECONDS);
        timeouts.put(internalKey, f);
    }

    /** 외부에서 수동 종료 시 예약 취소 */
    public void cancelTimeout(String internalKey) {
        ScheduledFuture<?> f = timeouts.remove(internalKey);
        if (f != null) f.cancel(false);
    }

    /** TTL 연장(갱신) */
    public void renewTimeout(Runnable stopAction, String internalKey, Duration ttl) {
        armTimeout(stopAction, internalKey, ttl);
    }
}
