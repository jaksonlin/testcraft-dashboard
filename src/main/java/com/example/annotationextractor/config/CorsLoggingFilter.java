package com.example.annotationextractor.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Component
@Order(1) // Run before other filters
public class CorsLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CorsLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI();
        String origin = httpRequest.getHeader("Origin");
        String accessControlRequestMethod = httpRequest.getHeader("Access-Control-Request-Method");
        String accessControlRequestHeaders = httpRequest.getHeader("Access-Control-Request-Headers");

        // Log all requests, especially OPTIONS (preflight)
        if ("OPTIONS".equals(method) || path.contains("/sse") || path.contains("/mcp")) {
            logger.info("=== CORS Request ===");
            logger.info("Method: {}", method);
            logger.info("Path: {}", path);
            logger.info("Origin: {}", origin);
            logger.info("Access-Control-Request-Method: {}", accessControlRequestMethod);
            logger.info("Access-Control-Request-Headers: {}", accessControlRequestHeaders);
            
            // Log all headers
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            logger.info("All Headers:");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                logger.info("  {}: {}", headerName, httpRequest.getHeader(headerName));
            }
        }

        chain.doFilter(request, response);

        // Log response headers after the filter chain
        if ("OPTIONS".equals(method) || path.contains("/sse") || path.contains("/mcp")) {
            logger.info("=== CORS Response ===");
            logger.info("Status: {}", httpResponse.getStatus());
            logger.info("Access-Control-Allow-Origin: {}", httpResponse.getHeader("Access-Control-Allow-Origin"));
            logger.info("Access-Control-Allow-Methods: {}", httpResponse.getHeader("Access-Control-Allow-Methods"));
            logger.info("Access-Control-Allow-Headers: {}", httpResponse.getHeader("Access-Control-Allow-Headers"));
            logger.info("Access-Control-Allow-Credentials: {}", httpResponse.getHeader("Access-Control-Allow-Credentials"));
        }
    }
}

