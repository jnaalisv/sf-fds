package com.github.jnaalisv.sf_fds.http.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates a request from the {@value #HEADER} header. A missing or unknown key
 * leaves the request unauthenticated; the authorization rules then reject it with 401.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    private final ApiKeyRegistry apiKeyRegistry;
    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();

    public ApiKeyAuthenticationFilter(ApiKeyRegistry apiKeyRegistry) {
        this.apiKeyRegistry = apiKeyRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER);
        if (apiKey != null) {
            apiKeyRegistry.clientFor(apiKey).ifPresentOrElse(
                    client -> {
                        SecurityContext context = contextHolder.createEmptyContext();
                        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                                client, null, AuthorityUtils.createAuthorityList("ROLE_API_CLIENT")));
                        contextHolder.setContext(context);
                    },
                    () -> log.info("Rejected unknown API key on {} {}", request.getMethod(), request.getRequestURI()));
        }
        chain.doFilter(request, response);
    }
}
