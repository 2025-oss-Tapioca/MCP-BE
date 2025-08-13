package com.tapioca.MCPBE.service.service;

import com.tapioca.MCPBE.domain.dto.result.GitPushResultDto;
import com.tapioca.MCPBE.domain.entity.GitHubEntity;
import com.tapioca.MCPBE.exception.CustomException;
import com.tapioca.MCPBE.exception.ErrorCode;
import com.tapioca.MCPBE.repository.repo.GitHubRepository;
import com.tapioca.MCPBE.service.usecase.GitPushUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GitPushService implements GitPushUseCase {

    private final GitHubRepository gitHubRepository;

    @Override
    public GitPushResultDto execute(String teamCode) {

        if (teamCode == null || teamCode.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_CODE);
        }

        try {

            GitHubEntity githubEntity = gitHubRepository.findByTeamCode(teamCode);

            String pat = githubEntity.getAccessToken();

            return new GitPushResultDto(pat);

        } catch (Exception e) {

            throw new CustomException(ErrorCode.NOT_FOUND_GITHUB);
        }

    }
}
