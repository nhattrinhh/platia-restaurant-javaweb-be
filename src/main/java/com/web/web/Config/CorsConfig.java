package com.web.web.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho tất cả API
                        .allowedOrigins(
                                "https://nhat.cloud",
                                "https://api.nhat.cloud",
                                "http://localhost:5173",
                                "http://localhost:3000") // Thêm các origin của Front-end
                        .allowedMethods("*")// Các phương thức được phép "GET", "POST", "PUT", "DELETE", "OPTIONS"
                        .allowedHeaders("*") // Cho phép tất cả headers
                        .allowCredentials(true);// Cho phép cookie, authorization headers, v.v.
            }
        };
    }
}
