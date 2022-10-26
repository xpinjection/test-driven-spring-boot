package com.xpinjection.library.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class ActuatorBasicSecurityConfig {
    private WebEndpointProperties webEndpointProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var authorizeRequests = http.authorizeRequests();
        if (webEndpointProperties != null) {
            authorizeRequests
                    .antMatchers(webEndpointProperties.getBasePath().concat("/health/**")).permitAll()
                    .antMatchers(webEndpointProperties.getBasePath().concat("/**")).hasRole("ADMIN");
        }
        authorizeRequests
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .httpBasic()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .csrf()
                .disable();
        return http.build();
    }

    @Autowired(required = false)
    public void setWebEndpointProperties(WebEndpointProperties webEndpointProperties) {
        this.webEndpointProperties = webEndpointProperties;
    }
}