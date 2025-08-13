package com.tapioca.MCPBE.repository.repo;

import com.tapioca.MCPBE.domain.entity.GitHubEntity;

public interface GitHubRepository {
    public GitHubEntity findByTeamCode(String teamCode);
}
