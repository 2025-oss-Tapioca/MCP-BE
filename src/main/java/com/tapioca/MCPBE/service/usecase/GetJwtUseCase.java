package com.tapioca.MCPBE.service.usecase;

public interface GetJwtUseCase {
    public String getJwtFromLogin(String loginId, String password,String loginPath);
}
