package com.tapioca.MCPBE.service.service;

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
    public CodeSkeletonResultDto execute(String teamCode) {

        if (teamCode == null || teamCode.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_CODE);
        }

        try {

            ErdEntity teamErd = erdRepository.findByTeamCode(teamCode);

            return new CodeSkeletonResultDto(teamCode, teamErd);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.NOT_FOUND_ERD);
        }
    }
}
