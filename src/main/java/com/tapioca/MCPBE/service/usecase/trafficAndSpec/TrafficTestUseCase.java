package com.tapioca.MCPBE.service.usecase.trafficAndSpec;

import com.tapioca.MCPBE.domain.dto.result.TrafficTestResultDto;

public interface TrafficTestUseCase {
    TrafficTestResultDto execute(String output);
}
