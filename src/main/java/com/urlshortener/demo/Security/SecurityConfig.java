package com.urlshortener.demo.Security;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configurable
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())   //Commonly disabled for API endpoints
                .authorizeHttpRequests(auth -> auth
                                //Public endpoints here
                                .requestMatchers("/api/shorten/**").permitAll()
                                .requestMatchers("/api/redirect/**").permitAll()
                                //Secured enpoints here
                                .requestMatchers("/api/analytics/**").hasRole("ADMIN")

                                //Any request that doesn't match the above rules will require authentication (but no specific role)
                                .anyRequest().authenticated()
                                //Could be more restrictive if desired, and do:
                                //.anyRequest().denyAll();
                                //Or could be more permissive and do the opposite (permitAll()).
                )
                //JWT implementation
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

}
