package com.asre.asre.infra.security;

import com.asre.asre.application.apikey.ApiKeyService;
import com.asre.asre.application.ratelimit.RateLimiterService;
import com.asre.asre.domain.ingestion.ApiMetricsCollectorPort;
import com.asre.asre.domain.project.Project;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;
    private final RateLimiterService rateLimiterService;
    private final ApiMetricsCollectorPort apiMetricsCollector;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate API key and get project
        var projectOpt = apiKeyService.getProjectByApiKey(apiKey);
        if (projectOpt.isEmpty()) {
            apiMetricsCollector.recordInvalidApiKey();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid API key\"}");
            return;
        }

        Project project = projectOpt.get();

        // Check rate limit
        if (!rateLimiterService.isWithinLimit(project)) {
            apiMetricsCollector.recordRateLimitExceeded();
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }

        apiMetricsCollector.recordApiRequest();

        // Set authentication context
        UUID projectId = project.getId();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                projectId.toString(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_KEY")));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Add project_id to request attributes for easy access
        request.setAttribute("projectId", projectId);

        filterChain.doFilter(request, response);
    }
}
