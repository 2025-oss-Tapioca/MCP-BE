// com.tapioca.MCPBE.mcp.BeClientConfig
package com.tapioca.MCPBE.mcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeClientConfig {

    @Value("${be.base-url}") // 예: http://localhost:18080
    private String beBaseUrl;

    @Bean(name = "beWebClient")
    public WebClient beWebClient() {
        return WebClient.builder().baseUrl(beBaseUrl).build();
    }
}
