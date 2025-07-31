package com.tapioca.MCPBE.repository.impl;

import com.tapioca.MCPBE.domain.entity.BackEntity;
import com.tapioca.MCPBE.repository.jpa.BackJpaRepository;
import com.tapioca.MCPBE.repository.repo.BackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BackRepositoryImpl implements BackRepository {

    private final BackJpaRepository backJpaRepository;

    @Override
    public BackEntity findByEc2Host(String ec2Host){
        return backJpaRepository.findByEc2Host(ec2Host);
    }
}
