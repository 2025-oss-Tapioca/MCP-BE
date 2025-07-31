package com.tapioca.MCPBE.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "back")
public class BackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "back_id")
    private UUID id;

    @Column(name = "back_ec2_host")
    private String ec2Host;

    @Column(name = "back_ec2_url")
    private String ec2Url;

    @Column(name = "back_auth_token")
    private String authToken;

    @Column(name = "back_os")
    private String os;

    @Column(name = "back_env")
    private String env;
}