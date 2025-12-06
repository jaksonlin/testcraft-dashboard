package com.example.annotationextractor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web configuration for CORS and other web settings
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // NOTE:
        // The application already uses a servlet context-path of "/api" (see application.yml).
        // CORS mappings here are defined *after* the context-path, so they should NOT start with "/api".
        // Using "/**" ensures all API endpoints under the context-path participate in CORS.
        registry.addMapping("/**")
                .allowedOrigins("http://localhost", "http://127.0.0.1",
                               "http://localhost:3000", "http://127.0.0.1:3000", 
                               "http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        
        registry.addMapping("/repositories/**")
                .allowedOrigins("http://localhost", "http://127.0.0.1",
                               "http://localhost:3000", "http://127.0.0.1:3000", 
                               "http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        
        registry.addMapping("/teams/**")
                .allowedOrigins("http://localhost", "http://127.0.0.1",
                               "http://localhost:3000", "http://127.0.0.1:3000", 
                               "http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        
        // MCP endpoints: allow all origins since MCP clients can run from anywhere
        // Authentication is still required via JWT tokens
        registry.addMapping("/mcp/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        
        // SSE endpoints: allow all origins since MCP clients can run from anywhere
        // These endpoints are used for Server-Sent Events
        registry.addMapping("/sse/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight for 1 hour
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Default CORS configuration for most endpoints (localhost only)
        CorsConfiguration defaultConfiguration = new CorsConfiguration();
        defaultConfiguration.setAllowedOriginPatterns(Arrays.asList("http://localhost", "http://127.0.0.1",
                                                             "http://localhost:3000", "http://127.0.0.1:3000", 
                                                             "http://localhost:5173", "http://127.0.0.1:5173"));
        defaultConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        defaultConfiguration.setAllowedHeaders(Arrays.asList("*"));
        defaultConfiguration.setAllowCredentials(true);
        
        // MCP endpoints: allow all origins (MCP clients can run from anywhere)
        // Authentication is still required via JWT tokens
        CorsConfiguration mcpConfiguration = new CorsConfiguration();
        mcpConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
        mcpConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        mcpConfiguration.setAllowedHeaders(Arrays.asList("*"));
        mcpConfiguration.setAllowCredentials(true);
        
        // SSE endpoints: allow all origins (MCP clients can run from anywhere)
        // These endpoints are used for Server-Sent Events
        CorsConfiguration sseConfiguration = new CorsConfiguration();
        sseConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
        sseConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        sseConfiguration.setAllowedHeaders(Arrays.asList("*"));
        sseConfiguration.setAllowCredentials(true);
        sseConfiguration.setMaxAge(3600L); // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Same reasoning as above: path patterns here are relative to the servlet context-path,
        // so they should not be prefixed with "/api". Use "/**" so all endpoints are covered.
        source.registerCorsConfiguration("/**", defaultConfiguration);
        source.registerCorsConfiguration("/mcp/**", mcpConfiguration);
        source.registerCorsConfiguration("/sse/**", sseConfiguration);
        return source;
    }
}
