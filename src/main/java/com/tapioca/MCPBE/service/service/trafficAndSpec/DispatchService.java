package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.GetJwtUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.SpecTestUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.DispatchUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.TrafficTestUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DispatchService implements DispatchUseCase {

    private final TrafficTestUseCase trafficTestUseCase;
    private final SpecTestUseCase specTestUseCase;
    private final GetJwtUseCase getJwtUseCase;
    private final VegetaUseCase vegetaUseCase;

    @Override
    public Object execute(JsonNode json){
        String tool = json.get("tool").asText();
        String action = json.get("action").asText();

        JsonNode param = json.get("params").get("param");

        String method = param.get("method").asText();
        String url = param.get("url").asText();
        int rate = param.get("rate").asInt();
        int duration = param.get("duration").asInt();
        String loginId = param.get("loginId").asText();
        String password = param.get("password").asText();
        String loginPath = param.get("loginPath").asText();
        JsonNode jsonBody = param.get("jsonBody");

        String jwt = null;
        if (loginId != null && password != null && loginPath != null) {
            jwt = getJwtUseCase.getJwtFromLogin(loginId, password, loginPath);
        }

        try{
            String targetPath = vegetaUseCase.makeTargetFile(method,url,jwt,jsonBody);
            String output = vegetaUseCase.runVegeta(targetPath, rate, duration);

            return switch (action) {
                case "traffic_test" -> trafficTestUseCase.execute(output);
                case "spec_test" -> specTestUseCase.execute(output);
                default -> throw new IllegalArgumentException("Unknown action: " + action);
            };
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_MAPPING_VALUE);
        }
    }
}
