package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.domain.dto.result.TrafficTestResultDto;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.TrafficTestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TrafficTestService implements TrafficTestUseCase {

    private final ObjectMapper mapper;

    @Override
    public TrafficTestResultDto execute(String output){
        try{
            JsonNode root = mapper.readTree(output);
            JsonNode data = root.get("data").get("data");

            JsonNode requests = data.get("request");
            JsonNode bytes = data.path("bytes");

            return new TrafficTestResultDto(requests, bytes);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_MAPPING_VALUE);
        }
    }

}
