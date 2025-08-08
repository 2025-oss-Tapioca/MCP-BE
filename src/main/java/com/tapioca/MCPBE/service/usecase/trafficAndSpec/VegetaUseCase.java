package com.tapioca.MCPBE.service.usecase.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface VegetaUseCase {
    public String makeTargetFile(String method, String url, String jwt, JsonNode apiDto)  throws IOException;
    public String runVegeta(String targetPath, int rate, int duration);
}
