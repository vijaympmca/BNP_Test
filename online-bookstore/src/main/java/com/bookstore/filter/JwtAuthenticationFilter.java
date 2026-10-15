package com.bookstore.filter;

import com.bookstore.pojo.JwtClaims;
import com.bookstore.security.CustomUserDetailsService;
import com.bookstore.security.JwtTokenProvider;
import com.bookstore.utility.AppConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads "Authorization: Bearer &lt;token&gt;", validates it and puts the user into the
 * SecurityContext. If the token is missing or invalid the request continues
 * unauthenticated; Spring Security then returns 401 for protected routes.
 * <p>
 * The user is re-loaded from the database so that a disabled account or a role
 * change made by an ADMIN takes effect immediately, not only when the token expires.
 * <p>
 * Registered only inside the security chain (see SecurityConfig), not as a servlet filter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(AppConstants.AUTH_HEADER);
        if (header != null && header.startsWith(AppConstants.BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(AppConstants.BEARER_PREFIX.length()).trim();
            try {
                JwtClaims claims = jwtTokenProvider.parse(token);
                UserDetails user = userDetailsService.loadUserByUsername(claims.username());
                if (user.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    request.setAttribute(AppConstants.JWT_ERROR_ATTRIBUTE, "Account is disabled. Please contact support");
                }
            } catch (ExpiredJwtException ex) {
                request.setAttribute(AppConstants.JWT_ERROR_ATTRIBUTE, "Token has expired. Please log in again");
            } catch (JwtException | IllegalArgumentException ex) {
                request.setAttribute(AppConstants.JWT_ERROR_ATTRIBUTE, "Invalid token");
            } catch (UsernameNotFoundException ex) {
                request.setAttribute(AppConstants.JWT_ERROR_ATTRIBUTE, "User of this token no longer exists");
            }
        }
        chain.doFilter(request, response);
    }
}
