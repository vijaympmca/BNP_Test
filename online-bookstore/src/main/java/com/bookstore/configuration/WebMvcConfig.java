package com.bookstore.configuration;

import com.bookstore.bean.AppProperties;
import com.bookstore.interceptor.AuditInterceptor;
import com.bookstore.interceptor.ExecutionTimeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registers the interceptors and CORS for a browser frontend. */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ExecutionTimeInterceptor executionTimeInterceptor;
    private final AuditInterceptor auditInterceptor;
    private final AppProperties appProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(executionTimeInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(auditInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(appProperties.corsAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Correlation-Id", "Location")
                .maxAge(3600);
    }
}
