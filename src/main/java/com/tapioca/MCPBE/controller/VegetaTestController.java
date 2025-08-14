package com.tapioca.MCPBE.controller;

import com.tapioca.MCPBE.domain.dto.request.TestVegetaRequestDto;
import com.tapioca.MCPBE.domain.dto.result.TrafficTestResultDto;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.TrafficTestUseCase;
import com.tapioca.MCPBE.service.usecase.trafficAndSpec.VegetaCommonUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequiredArgsConstructor
public class VegetaTestController {
    private final VegetaCommonUseCase vegetaCommonUseCase; //String
    private final TrafficTestUseCase trafficTestUseCase; //TrafficTestResultDto

    @PostMapping("/son")
    public TrafficTestResultDto testVegeta(
            @RequestBody TestVegetaRequestDto testVegetaRequestDto
    ){
        System.out.println("test");
        return trafficTestUseCase.execute(
                testVegetaRequestDto.method(),
                testVegetaRequestDto.url(),
                testVegetaRequestDto.loginPath(),
                testVegetaRequestDto.loginId(),
                testVegetaRequestDto.password(),
                testVegetaRequestDto.rate(),
                testVegetaRequestDto.duration(),
                testVegetaRequestDto.jsonBody()
        );
    }
}
