package com.ozwillo.socatelgraphql.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().and()
                .requestMatcher(EndpointRequest.to(Health.class)).authorizeRequests().anyRequest().permitAll().and()
                .antMatcher("/graphql").authorizeRequests().anyRequest().fullyAuthenticated().and()
                .csrf().disable();
    }
}
