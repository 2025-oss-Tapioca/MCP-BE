package com.tapioca.MCPBE.service.usecase;

import java.util.Map;

public interface LogCollectorUseCase {
    /** 비동기 시작. 이미 실행 중이면 no-op */
    void start(
            String internalKey,
            String type,
            String teamCode,
            String callbackUrl,
            Map<String,Object> cfg);

    /** 중단(선택사항). 필요 없으면 호출 안 해도 됨 */
    void stop(String internalKey);
}
