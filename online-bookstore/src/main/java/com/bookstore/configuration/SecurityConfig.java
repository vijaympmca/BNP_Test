package com.bookstore.configuration;

import com.bookstore.filter.JwtAuthenticationFilter;
import com.bookstore.security.CustomAccessDeniedHandler;
import com.bookstore.security.CustomUserDetailsService;
import com.bookstore.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Route rules for the two roles:
 * <pre>
 *  Public       : POST /api/auth/register, POST /api/auth/login, GET /api/books/**,
 *                 /actuator/health, /h2-console/**, Swagger
 *  ADMIN only   : /api/admin/**, POST/PUT/DELETE /api/books/**
 *  USER + ADMIN : everything else (cart, orders, /api/auth/me)
 * </pre>
 * Ant matchers are used because the H2 console is a separate servlet.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)                       // stateless API, no cookies
                .cors(Customizer.withDefaults())                             // uses WebMvcConfig CORS mappings
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))           // H2 console uses frames (local only)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)  // 401 JSON
                        .accessDeniedHandler(accessDeniedHandler))           // 403 JSON
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/auth/register"),
                                antMatcher(HttpMethod.POST, "/api/auth/login")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/books/**")).permitAll()
                        .requestMatchers(antMatcher("/actuator/health/**"), antMatcher("/actuator/info")).permitAll()
                        .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(antMatcher("/v3/api-docs/**"), antMatcher("/swagger-ui/**"),
                                antMatcher("/swagger-ui.html")).permitAll()
                        .requestMatchers(antMatcher("/error")).permitAll()
                        // administrator only
                        .requestMatchers(antMatcher("/api/admin/**")).hasRole("ADMIN")
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/books/**"),
                                antMatcher(HttpMethod.PUT, "/api/books/**"),
                                antMatcher(HttpMethod.DELETE, "/api/books/**")).hasRole("ADMIN")
                        // any logged-in user (USER or ADMIN)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Strategy pattern: BCrypt can be swapped for another encoder without touching services. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /** Stop Spring Boot from also registering the JWT filter as a plain servlet filter (it would run twice). */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
