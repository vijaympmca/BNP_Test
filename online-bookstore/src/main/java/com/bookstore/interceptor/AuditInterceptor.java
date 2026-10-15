package com.bookstore.interceptor;

import com.bookstore.bean.RequestContext;
import com.bookstore.utility.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Fills the request-scoped RequestContext and writes an audit line for every
 * state-changing call (who did what, and with which result).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

    private static final Set<String> AUDITED_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final RequestContext requestContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object correlationId = request.getAttribute(AppConstants.CORRELATION_ID_MDC_KEY);
        requestContext.setCorrelationId(correlationId == null ? null : correlationId.toString());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            requestContext.setUsername(auth.getName());
            requestContext.setRole(auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).findFirst().orElse(null));
        } else {
            requestContext.setUsername("anonymous");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (AUDITED_METHODS.contains(request.getMethod())) {
            log.info("AUDIT user={} role={} action={} {} status={}",
                    requestContext.getUsername(), requestContext.getRole(),
                    request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }
}
