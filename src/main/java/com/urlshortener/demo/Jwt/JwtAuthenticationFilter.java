package com.urlshortener.demo.Jwt;

import com.urlshortener.demo.User.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String requestPath = request.getServletPath();

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                if (!shouldNotFilter(request)) {
                    logger.warn("No Bearer token found for protected endpoint: {}", requestPath);
                } else {
                    logger.info("Public endpoint accessed: {}", requestPath);
                }
                filterChain.doFilter(request, response);
                return;
            }


            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            logger.info("Processing token for username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("User authorities: {}", userDetails.getAuthorities());

                if (jwtService.isTokenExpired(jwt)) {
                    logger.info("Token has expired");
                    throw new ServletException("Token has expired");
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication successful");
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed: " + e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.equals("/") ||
                path.equals("/error") ||
                path.equals("/404") ||
                path.equals("/index.html") ||
                path.equals("/register.html") ||
                path.equals("/login.html") ||
                path.equals("/forgot-password.html") ||
                path.equals("/profile.html") ||
                path.startsWith("/static/") ||
                path.endsWith(".js") ||
                path.endsWith(".css") ||
                path.equals("/api/users/forgot-password") ||
                path.startsWith("/api/password/reset-password") ||
                path.equals("/api/urls/generate") ||
                path.startsWith("/api/urls/redirect/") ||
                path.equals("/api/users/register") ||
                path.equals("/api/auth/login") ||
                path.startsWith("/api/urls/qr/") ||
                path.equals("/api/urls/app-url");
    }

}