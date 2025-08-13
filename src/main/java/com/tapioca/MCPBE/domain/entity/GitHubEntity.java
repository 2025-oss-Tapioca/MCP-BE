package com.tapioca.MCPBE.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name="github_config")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="github_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TeamEntity teamEntity;

    @Column(name = "github_repo_url", nullable = false)
    private String repoUrl;

    @Column(name = "github_is_private", nullable = false)
    private boolean isPrivate;

    @Column(name = "github_access_token", nullable = false)
    private String accessToken;

    @Column(name = "github_default_branch", nullable = false)
    private String defaultBranch;
}
