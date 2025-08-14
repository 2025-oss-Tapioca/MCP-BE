package com.tapioca.MCPBE.service.usecase.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;

public interface VegetaCommonUseCase {
    public String execute(String method, String url, String loginPath,String loginId,
                        String password, int rate, int duration, JsonNode jsonBody);
}
