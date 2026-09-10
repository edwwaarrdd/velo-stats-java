package com.velostats.http;

import com.velostats.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Cross-origin access for the browser client.
 *
 * <p>The policy is the same across every velo-stats backend: a configured list of origins, any
 * method, any header, no credentials. That is small enough to express directly, and doing so keeps
 * the origin list readable from the environment.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    private final List<String> allowedOrigins;

    public CorsFilter(AppProperties properties) {
        this.allowedOrigins = properties.corsAllowedOrigins();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String origin = request.getHeader("Origin");

        // The header echoes the configured entry rather than the request's own value, so what
        // reaches the response provably comes from configuration.
        String allowedOrigin = allowedOrigins.stream()
                .filter(candidate -> candidate.equals(origin))
                .findFirst()
                .orElse(null);

        if (allowedOrigin != null) {
            response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setHeader("Access-Control-Allow-Headers", "*");

            // Responses differ by origin, so a shared cache must not serve one origin's response to
            // another.
            response.setHeader("Vary", "Origin");
        }

        // A preflight request is answered here and never reaches a handler, which is the whole point
        // of preflight: it asks about the endpoint rather than calling it.
        if (origin != null && "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

            return;
        }

        chain.doFilter(request, response);
    }
}
