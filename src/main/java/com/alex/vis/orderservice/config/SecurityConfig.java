package com.alex.vis.orderservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //We share security context on threads which spawned by other processes
    @PostConstruct
    public void enabledAuthenticationContextOnSpawnedThreads() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
