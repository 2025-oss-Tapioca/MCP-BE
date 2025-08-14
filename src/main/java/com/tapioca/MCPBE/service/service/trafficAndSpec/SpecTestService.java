package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.domain.dto.result.SpecTestResultDto;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaCommonUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.SpecTestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SpecTestService implements SpecTestUseCase {

    private final ObjectMapper mapper;
    private final VegetaCommonUseCase vegetaCommonService;

    @Override
    public SpecTestResultDto execute(String method, String url, String loginPath, String loginId,
                                     String password, int rate, int duration, JsonNode jsonBody){
        try {
            String output = vegetaCommonService.execute(method, url, loginPath, loginId
                    , password, rate, duration, jsonBody);

            JsonNode root = mapper.readTree(output);

            JsonNode latencies = root.path("latencies");

            JsonNode resultDuration = root.path("duration");

            double throughput = root.path("throughput").asDouble(0.0);

            double success = root.path("success").asDouble(0.0);
            String successRatio = String.format(java.util.Locale.ROOT, "%.2f%%", success * 100.0);

            JsonNode statusCodes = root.path("status_codes");
            if (statusCodes.isMissingNode() || statusCodes.isNull()) {
                statusCodes = mapper.createObjectNode();
            }

            return new SpecTestResultDto(latencies, resultDuration, throughput, successRatio, statusCodes);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}
