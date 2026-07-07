package com.criczone.demo.support;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestTracingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_ATTRIBUTE = "requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(RequestTracingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        String requestId = resolveRequestId(request);
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        MDC.put(REQUEST_ID_ATTRIBUTE, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            MDC.remove(REQUEST_ID_ATTRIBUTE);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String existing = request.getHeader(REQUEST_ID_HEADER);
        return existing == null || existing.isBlank() ? UUID.randomUUID().toString() : existing;
    }
}
