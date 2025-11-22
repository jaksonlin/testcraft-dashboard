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
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost", "http://127.0.0.1",
                                                             "http://localhost:3000", "http://127.0.0.1:3000", 
                                                             "http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Same reasoning as above: path patterns here are relative to the servlet context-path,
        // so they should not be prefixed with "/api". Use "/**" so all endpoints are covered.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
