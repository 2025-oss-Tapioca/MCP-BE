package com.tapioca.MCPBE.service.service;

import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.LogCollectorUseCase;
import com.tapioca.MCPBE.util.common.LogLineParser;
import com.tapioca.MCPBE.util.websocket.BeWsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudWatchCollectorService implements LogCollectorUseCase {

    private final BeWsClient ws;
    private final LogLineParser parser = new LogLineParser();
    private final LogCollectionSupervisor supervisor;   // ✅ 30분 자동 종료 스케줄러

    private final ExecutorService exec = Executors.newCachedThreadPool();
    private final ConcurrentMap<String, Future<?>> running = new ConcurrentHashMap<>();

    @Override
    public void start(String internalKey, String type, String teamCode, String callbackUrl, Map<String, Object> cfg) {
        running.computeIfAbsent(internalKey, k -> {
            Future<?> f = exec.submit(() -> runPoll(internalKey, type, teamCode, callbackUrl, cfg));
            // ✅ 시작과 동시에 기본 TTL(설정값, 기본 30분) 타임아웃 예약
            supervisor.armDefaultTimeout(() -> stop(internalKey), internalKey);
            return f;
        });
    }

    @Override
    public void stop(String internalKey) {
        supervisor.cancelTimeout(internalKey); // ✅ 예약 취소
        Future<?> f = running.remove(internalKey);
        if (f != null) f.cancel(true);
    }

    private void runPoll(String internalKey, String type, String teamCode, String callbackUrl, Map<String, Object> cfg) {
        final String regionStr = val(cfg, "awsRegion", "ap-northeast-2");
        final String instanceId = val(cfg, "rdsInstanceId", null);
        final String logGroup = val(cfg, "logGroup",
                (instanceId != null) ? "/aws/rds/instance/" + instanceId + "/postgresql" : null);

        if (logGroup == null) throw new CustomException(ErrorCode.INVALID_REQUEST_BODY);

        ws.connectAndRegister(internalKey, callbackUrl, type, teamCode);

        Region region = Region.of(regionStr);
        try (CloudWatchLogsClient cw = CloudWatchLogsClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            String streamName = mostRecentStream(cw, logGroup);
            if (streamName == null) throw new CustomException(ErrorCode.LOG_STREAM_NOT_FOUND);

            long startMillis = Instant.now().toEpochMilli() - 5_000;
            String nextToken = null;

            while (!Thread.currentThread().isInterrupted()) {
                FilterLogEventsRequest req = FilterLogEventsRequest.builder()
                        .logGroupName(logGroup)
                        .logStreamNames(streamName)
                        .startTime(startMillis)
                        .nextToken(nextToken)
                        .limit(1000)
                        .build();

                FilterLogEventsResponse res;
                try {
                    res = cw.filterLogEvents(req);
                } catch (Exception e) {
                    throw new CustomException(ErrorCode.LOG_CLOUDWATCH_SUBSCRIBE_FAILED);
                }

                for (FilteredLogEvent ev : res.events()) {
                    startMillis = Math.max(startMillis, ev.timestamp() + 1);
                    ws.sendLog(internalKey, parser.toMap(ev.message(), type, teamCode, ev.timestamp()));
                }
                nextToken = res.nextToken();

                Thread.sleep(1500);
            }
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("[CW] connect failed {}: {}", internalKey, e.toString());
            throw new CustomException(ErrorCode.LOG_CLOUDWATCH_CONNECTION_FAILED);
        }
    }

    private static String mostRecentStream(CloudWatchLogsClient cw, String logGroup) {
        DescribeLogStreamsResponse res = cw.describeLogStreams(DescribeLogStreamsRequest.builder()
                .logGroupName(logGroup)
                .orderBy(OrderBy.LAST_EVENT_TIME)
                .descending(true)
                .limit(1)
                .build());
        return res.logStreams().isEmpty() ? null : res.logStreams().get(0).logStreamName();
    }

    private static String val(Map<String,Object> cfg, String key, String def){
        Object v = (cfg==null? null : cfg.get(key));
        return v==null? def : String.valueOf(v);
    }
}
