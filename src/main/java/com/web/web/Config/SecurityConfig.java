package com.web.web.Config;

import com.web.web.Entity.Role;
import com.web.web.Entity.User;
import com.web.web.Repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public SecurityConfig(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username);
            if (user == null)
                throw new UsernameNotFoundException("User not found");
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                    .build();
        };
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Bật CORS với cấu hình từ corsConfigurationSource()
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Tắt CSRF vì API không dùng form
                .csrf(csrf -> csrf.disable())
                // Stateless session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Quyền truy cập endpoint
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/products", "/api/products/search").permitAll()
                        .requestMatchers("/api/product-types/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/chatbot").permitAll()
                        .requestMatchers("/api/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/profile").permitAll()
                        .requestMatchers("/api/admin/profile").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasRole("ADMIN")
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/news", "/api/news/search").permitAll()
                        .requestMatchers("/api/news/**").hasRole("ADMIN")
                        .requestMatchers("/api/booking/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/statistics/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Xử lý lỗi 401 và 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, exAuth) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Chưa đăng nhập hoặc token không hợp lệ\"}");
                        })
                        .accessDeniedHandler((req, res, accessEx) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Không có quyền truy cập\"}");
                        }))
                // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://nhat.cloud", "https://www.nhat.cloud"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
