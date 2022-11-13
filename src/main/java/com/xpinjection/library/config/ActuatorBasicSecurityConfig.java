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
        return http.authorizeRequests(requests ->
                        requests.antMatchers(actuatorBasePath + "/health/**").permitAll()
                                .antMatchers(actuatorBasePath + "/**").hasRole("ADMIN")
                                .antMatchers("/**").permitAll()
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