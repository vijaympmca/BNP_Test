package com.bookstore.interceptor;

import com.bookstore.bean.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** Measures controller execution time and warns about slow requests. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionTimeInterceptor implements HandlerInterceptor {

    private static final String START_ATTRIBUTE = ExecutionTimeInterceptor.class.getName() + ".start";

    private final AppProperties appProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_ATTRIBUTE, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object start = request.getAttribute(START_ATTRIBUTE);
        if (!(start instanceof Long startNanos)) {
            return;
        }
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        String target = handler instanceof HandlerMethod hm
                ? hm.getBeanType().getSimpleName() + "." + hm.getMethod().getName()
                : request.getRequestURI();
        if (elapsedMs > appProperties.slowRequestThresholdMs()) {
            log.warn("SLOW handler {} took {} ms", target, elapsedMs);
        } else {
            log.debug("Handler {} took {} ms", target, elapsedMs);
        }
    }
}
