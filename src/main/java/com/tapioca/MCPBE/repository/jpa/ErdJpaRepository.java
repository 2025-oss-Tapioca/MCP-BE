package com.tapioca.MCPBE.repository.jpa;

import com.tapioca.MCPBE.domain.entity.erd.ErdEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ErdJpaRepository extends JpaRepository<ErdEntity, UUID> {
    @EntityGraph(attributePaths = {
            "diagrams",
            "diagrams.attributes",
            "attributeLinks",
            "attributeLinks.fromAttribute",
            "attributeLinks.toAttribute"
    })
    public ErdEntity findByTeamEntity_code(String teamCode);
}
