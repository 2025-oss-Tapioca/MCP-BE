package com.tapioca.MCPBE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class McpbeApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpbeApplication.class, args);
	}

}
