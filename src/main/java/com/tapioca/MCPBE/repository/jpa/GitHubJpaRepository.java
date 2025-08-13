package com.tapioca.MCPBE.repository.jpa;

import com.tapioca.MCPBE.domain.entity.GitHubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GitHubJpaRepository extends JpaRepository<GitHubEntity, UUID> {
    Optional<GitHubEntity> findByTeamEntity_code(String teamCode);
}
