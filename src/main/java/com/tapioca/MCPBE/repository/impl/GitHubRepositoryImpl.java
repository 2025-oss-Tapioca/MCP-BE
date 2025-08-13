package com.tapioca.MCPBE.repository.impl;

import com.tapioca.MCPBE.domain.entity.GitHubEntity;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.repository.jpa.GitHubJpaRepository;
import com.tapioca.MCPBE.repository.repo.GitHubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GitHubRepositoryImpl implements GitHubRepository {

    private final GitHubJpaRepository gitHubJpaRepository;

    @Override
    public GitHubEntity findByTeamCode(String teamCode) {
        return gitHubJpaRepository.findByTeamEntity_code(teamCode)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_GITHUB));
    }
}
