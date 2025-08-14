package com.tapioca.MCPBE.service.service.trafficAndSpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.service.usecase.GetJwtUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaCommonUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VegetaCommonService implements VegetaCommonUseCase {

    private final GetJwtUseCase getJwtUseCase;
    private final VegetaUseCase vegetaUseCase;

    public String execute(String method, String url, String loginPath,String loginId,
                          String password, int rate, int duration, JsonNode jsonBody){
        String jwt = null;
        if (hasText(loginId) && hasText(password) && hasText(loginPath)) {
            jwt = getJwtUseCase.getJwtFromLogin(loginId, password, loginPath);
        }

        try{
            String targetPath = vegetaUseCase.makeTargetFile(method,url,jwt,jsonBody);
            String output = vegetaUseCase.runVegeta(targetPath, rate, duration);

            return output;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_MAPPING_VALUE);
        }
    }

    private boolean hasText(String s) { return s != null && !s.isBlank(); }
}
