package com.tapioca.MCPBE.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.TrafficTestUseCase;
import com.tapioca.MCPBE.service.usecase.BeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BeService implements BeUseCase {

    private final TrafficTestUseCase apiSpecTestUseCase;

    @Override
    public Object beRequest(JsonNode beJson) {
        String type = beJson.get("type").asText();
        JsonNode json = beJson;

        switch (type){
            case "traffic_test":
                return apiSpecTestUseCase.execute(json);
        }
        throw new CustomException(ErrorCode.TYPE_NOT_ALLOWED);
    }

}
