package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.domain.dto.result.SpecTestResultDto;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaCommonService;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.SpecTestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SpecTestService implements SpecTestUseCase {

    private final ObjectMapper mapper;
    private final VegetaCommonService vegetaCommonService;

    @Override
    public SpecTestResultDto execute(String method, String url, String loginPath, String loginId,
                                     String password, int rate, int duration, JsonNode jsonBody){
        try {
            String output = vegetaCommonService.execute(method, url, loginPath, loginId
                    , password, rate, duration, jsonBody);

            System.out.println("test output");
            System.out.println(output);
            System.out.println(mapper.writeValueAsString(output));
            JsonNode root = mapper.readTree(output);
            //JsonNode data = root.get("data").get("data");

            JsonNode latencies = root.path("latencies");
            JsonNode resultDuration = root.path("duration");
            double throughput = root.path("requests").path("throughput").asDouble();
            String successRatio = root.path("successRatio").asText();
            JsonNode statusCodes = root.path("statusCodes");

            return new SpecTestResultDto(latencies, resultDuration, throughput, successRatio, statusCodes);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}
