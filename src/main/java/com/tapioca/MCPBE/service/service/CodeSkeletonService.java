package com.tapioca.MCPBE.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapioca.MCPBE.domain.dto.result.CodeSkeletonResultDto;
import com.tapioca.MCPBE.domain.entity.erd.ErdEntity;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.repository.repo.ErdRepository;
import com.tapioca.MCPBE.service.usecase.CodeSkeletonUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CodeSkeletonService implements CodeSkeletonUseCase {

    private final ErdRepository erdRepository;

    @Override
    public CodeSkeletonResultDto execute(JsonNode jsonNode) {

        String teamCode = jsonNode.path("teamCode").asText();

        if(teamCode.isEmpty()){
            throw new CustomException(ErrorCode.NOT_FOUND_ERD);
        }

        ErdEntity teamErd = erdRepository.findByTeamEntity_code(teamCode);

        return new CodeSkeletonResultDto(teamCode, teamErd);
    }
}
