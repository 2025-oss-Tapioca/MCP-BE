package com.tapioca.MCPBE.repository.repo;

import com.tapioca.MCPBE.domain.entity.erd.ErdEntity;

public interface ErdRepository {
    public ErdEntity findByTeamEntity_code(String teamCode);
}
