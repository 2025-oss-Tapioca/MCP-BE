package com.tapioca.MCPBE.repository.jpa;

import com.tapioca.MCPBE.domain.entity.BackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BackJpaRepository extends JpaRepository<BackEntity,UUID> {
    public BackEntity findByEc2Host(String ec2Host);
}