package com.xpinjection.library.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class ActuatorBasicSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, WebEndpointProperties webEndpointProperties) throws Exception {
        var actuatorBasePath = webEndpointProperties.getBasePath();
        return http.authorizeHttpRequests(requests ->
                        requests.requestMatchers(actuatorBasePath + "/health/**").permitAll()
                                .requestMatchers(actuatorBasePath + "/**").hasRole("ADMIN")
                                .requestMatchers("/**").permitAll()
                                .anyRequest().authenticated())
            .httpBasic()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .csrf()
                .disable()
            .build();
    }
}