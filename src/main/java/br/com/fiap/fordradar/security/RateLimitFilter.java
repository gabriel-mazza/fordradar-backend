package br.com.fiap.fordradar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final int authLimit;
    private final int globalLimit;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${security.rate-limit.auth-per-minute:10}") int authLimit,
                           @Value("${security.rate-limit.global-per-minute:60}") int globalLimit) {
        this.authLimit = authLimit;
        this.globalLimit = globalLimit;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        if (windows.size() > MAX_TRACKED_KEYS) {
            windows.entrySet().removeIf(e -> now - e.getValue().start >= WINDOW_MS);
        }

        boolean authPath = request.getRequestURI().startsWith("/api/v1/auth/");
        if (authPath && !allow("auth:" + ip, authLimit, now)) {
            reject(request, response, ip);
            return;
        }
        if (!allow("global:" + ip, globalLimit, now)) {
            reject(request, response, ip);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean allow(String key, int limit, long now) {
        Window window = windows.compute(key,
                (k, current) -> (current == null || now - current.start >= WINDOW_MS) ? new Window(now) : current);
        return window.count.incrementAndGet() <= limit;
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String ip) throws IOException {
        AuditLog.event("RATE_LIMITED", "DENIED", "ip", ip, "path", request.getRequestURI());
        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Muitas requisições. Tente novamente em instantes.\"}");
    }

    private static final class Window {
        final long start;
        final AtomicInteger count = new AtomicInteger();

        Window(long start) {
            this.start = start;
        }
    }
}
