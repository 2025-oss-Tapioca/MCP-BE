package com.tapioca.MCPBE.repository.impl;

import com.tapioca.MCPBE.domain.entity.erd.ErdEntity;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.repository.jpa.ErdJpaRepository;
import com.tapioca.MCPBE.repository.repo.ErdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ErdRepositoryImpl implements ErdRepository {

    private final ErdJpaRepository erdJpaRepository;

    @Override
    public ErdEntity findByTeamCode(String teamCode) {
        return erdJpaRepository.findByTeamEntity_code(teamCode)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ERD));
    }
}
