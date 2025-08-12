package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tapioca.MCPBE.domain.dto.result.TrafficTestResultDto;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaCommonService;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.TrafficTestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TrafficTestService implements TrafficTestUseCase {

    private final ObjectMapper mapper;
    private final VegetaCommonService vegetaCommonService;

    @Override
    public TrafficTestResultDto execute(String method, String url, String loginPath, String loginId
                    , String password, int rate, int duration, JsonNode jsonBody){
        try{
            String output = vegetaCommonService.execute(method, url, loginPath, loginId
                    , password, rate, duration, jsonBody);
            JsonNode root = mapper.readTree(output);
//            JsonNode data = root.get("data").get("data");
//
//            JsonNode requests = data.get("request");
//            JsonNode bytes = data.path("bytes");

            JsonNode requests = root.path("requests"); // int
            // bytes_in / bytes_out 묶어서 DTO에 전달 (DTO가 기존 'bytes' 하나만 받는다면 이렇게 합쳐서 넘김)
            ObjectNode bytes = mapper.createObjectNode();
            bytes.set("in", root.path("bytes_in"));   // { total, mean }
            bytes.set("out", root.path("bytes_out")); // { total, mean }

            return new TrafficTestResultDto(requests, bytes);
        } catch (Exception e) {
            throw new RuntimeException("Load test 실행 실패: " + e.getMessage(), e);
            //throw new CustomException(ErrorCode.INVALID_MAPPING_VALUE);
        }
    }

}
