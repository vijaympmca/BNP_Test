package com.bookstore.bean;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * One instance per HTTP request (a scoped proxy is injected into singletons).
 * Filled by AuditInterceptor; any bean can read the correlation ID and current user from it.
 */
@Getter
@Setter
@Component
@RequestScope
public class RequestContext {
    private String correlationId;
    private String username;
    private String role;
}
