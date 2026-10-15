package com.bookstore.filter;

import com.bookstore.utility.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * First filter in the chain: assigns a correlation ID (or reuses the client's
 * X-Correlation-Id), puts it in the log MDC and response header, and logs each request.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String incoming = request.getHeader(AppConstants.CORRELATION_ID_HEADER);
        String correlationId = incoming != null && incoming.matches("^[A-Za-z0-9-]{1,64}$")
                ? incoming
                : UUID.randomUUID().toString();

        MDC.put(AppConstants.CORRELATION_ID_MDC_KEY, correlationId);
        request.setAttribute(AppConstants.CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(AppConstants.CORRELATION_ID_HEADER, correlationId);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(),
                    response.getStatus(), System.currentTimeMillis() - start);
            MDC.remove(AppConstants.CORRELATION_ID_MDC_KEY);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/h2-console") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");
    }
}
