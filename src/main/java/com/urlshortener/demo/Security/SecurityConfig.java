package com.urlshortener.demo.Security;

import com.urlshortener.demo.Jwt.JwtAuthenticationFilter;
import com.urlshortener.demo.User.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())   //Commonly disabled for API endpoints
                .authorizeHttpRequests(auth -> auth
                                //Public endpoints here
                                .requestMatchers("/").permitAll()                      // Allow access to root path
                                .requestMatchers("/r/**").permitAll()
                                .requestMatchers("/index.html").permitAll()           // Allow access to index.html
                                .requestMatchers("/register.html").permitAll()
                                .requestMatchers("/login.html").permitAll()
                                .requestMatchers("/forgot-password.html").permitAll()
                                .requestMatchers("/myurls-loader.html").permitAll()
                                .requestMatchers("/reset-password.html").permitAll()
                                .requestMatchers("/profile.html").permitAll()
                                .requestMatchers("/static/**").permitAll()
                                .requestMatchers("/*.js").permitAll()
                                .requestMatchers("/*.css").permitAll()
                                .requestMatchers("/*.js").permitAll()               // Allow JavaScript files
                                .requestMatchers("/*.css").permitAll()              // Allow CSS files
                                .requestMatchers("/favicon.ico").permitAll()        // Allow favicon
                                .requestMatchers("/error").permitAll()  // Spring Boot's default error handling
                                .requestMatchers("/404").permitAll()    // For custom 404 page if needed







                                .requestMatchers("/api/urls/generate").permitAll()
                                .requestMatchers("/api/urls/redirect/**").permitAll()
                                .requestMatchers("/api/users/register").permitAll()
                                .requestMatchers("/api/auth/login").permitAll()
                                .requestMatchers("/api/email/verify-email/**").permitAll()
                                .requestMatchers("/api/users/forgot-password/**").permitAll()
                                .requestMatchers("/api/password/reset-password/**").permitAll()
                                .requestMatchers("/api/urls/qr/**").permitAll()



                                //Secured endpoints here
                                .requestMatchers("/api/analytics/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/users/delete").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/myurls.html").hasAuthority("ROLE_USER")
                                .requestMatchers("/api/urls/user/**").hasAuthority("ROLE_USER")

                                //Any request that doesn't match the above rules will require authentication (but no specific role):
                                .anyRequest().authenticated()
                                //Could be more restrictive if desired, and do:
                                //.anyRequest().denyAll();
                                //Or could be more permissive and do the opposite:
                                //.anyRequest().permitAll()
                )
                //JWT implementation
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
