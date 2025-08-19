package com.tapioca.MCPBE.service.usecase;

import com.tapioca.MCPBE.domain.dto.request.LogLineDto;
import java.util.List;

public interface LogContextByLevelUseCase {
    List<LogLineDto> query(String sourceType, String teamCode, String level, Integer context);
}
