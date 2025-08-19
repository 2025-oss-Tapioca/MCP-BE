// com.tapioca.MCPBE.service.usecase.log.LogRangeQueryUseCase
package com.tapioca.MCPBE.service.usecase;

import com.tapioca.MCPBE.domain.dto.request.LogLineDto;
import java.util.List;

public interface LogRangeQueryUseCase {
    List<LogLineDto> query(String sourceType, String teamCode, String from, String to);
}
