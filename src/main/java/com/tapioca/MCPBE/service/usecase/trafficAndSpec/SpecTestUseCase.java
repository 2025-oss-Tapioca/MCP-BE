package com.tapioca.MCPBE.service.usecase.trafficAndSpec;

import com.tapioca.MCPBE.domain.dto.result.SpecTestResultDto;

public interface SpecTestUseCase {
    public SpecTestResultDto execute(String output);
}
