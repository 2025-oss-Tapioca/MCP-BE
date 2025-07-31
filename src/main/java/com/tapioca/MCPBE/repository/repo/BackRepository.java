package com.tapioca.MCPBE.repository.repo;

import com.tapioca.MCPBE.domain.entity.BackEntity;

import java.util.UUID;

public interface BackRepository {
    public BackEntity findByEc2Host(String ec2Host);
}
