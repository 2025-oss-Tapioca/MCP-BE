package com.tapioca.MCPBE.service.service;

import com.tapioca.MCPBE.domain.dto.be.BeRequestDto;
import com.tapioca.MCPBE.service.usecase.BeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BeService implements BeUseCase {

    @Override
    public void beRequest(BeRequestDto beRequestDto) {
        System.out.println("success");
    }

}
