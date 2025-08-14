package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tapioca.MCPBE.domain.dto.result.TrafficTestResultDto;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaCommonUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.TrafficTestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TrafficTestService implements TrafficTestUseCase {

    private final ObjectMapper mapper;
    private final VegetaCommonUseCase vegetaCommonService;

    @Override
    public TrafficTestResultDto execute(String method, String url, String loginPath, String loginId
                    , String password, int rate, int duration, JsonNode jsonBody){
        try{
            String output = vegetaCommonService.execute(method, url, loginPath, loginId
                    , password, rate, duration, jsonBody);
            System.out.println(output);
            JsonNode root = mapper.readTree(output);

            JsonNode requests = root.path("requests");
            ObjectNode bytes = mapper.createObjectNode();
            bytes.set("in", root.path("bytes_in"));
            bytes.set("out", root.path("bytes_out"));

            return new TrafficTestResultDto(requests, bytes);
        } catch (Exception e) {
            System.out.println("catch error");
            throw new CustomException(ErrorCode.INVALID_MAPPING_VALUE);
        }
    }

}
