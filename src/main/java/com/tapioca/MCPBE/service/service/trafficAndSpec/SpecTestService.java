package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.domain.dto.result.SpecTestResultDto;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.SpecTestUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SpecTestService implements SpecTestUseCase {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public SpecTestResultDto execute(String output){
        try {
            JsonNode root = mapper.readTree(output);
            JsonNode data = root.get("data").get("data");

            JsonNode latencies = data.path("latencies");
            JsonNode duration = data.path("duration");
            double throughput = data.path("requests").path("throughput").asDouble();
            String successRatio = data.path("successRatio").asText();
            JsonNode statusCodes = data.path("statusCodes");

            return new SpecTestResultDto(latencies, duration, throughput, successRatio, statusCodes);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}
