package com.bark.config;

import com.bark.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * 配置密码加密器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置JWT认证过滤器
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            // 禁用CSRF
            .csrf(csrf -> csrf.disable())
            // 不创建会话
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 定义请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 不需认证的接口
                .requestMatchers("/auth/login",
                        "/auth/captcha/**",
                        "/auth/checkInit", "/auth/initRegister",
                        "/notice/**", "/actuator/**",
                        "/push/**", "/status/endpoint").permitAll()
                // 所有其他请求需要认证
                .anyRequest().authenticated())
            // 配置异常处理器
            .exceptionHandling(ex -> ex
                // 未认证处理
                .authenticationEntryPoint((request, response, e) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":\"999999\",\"msg\":\"未授权，请登录\",\"data\":null}");
                })
                // 未授权处理
                .accessDeniedHandler((request, response, e) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":\"999999\",\"msg\":\"无权限访问\",\"data\":null}");
                }));

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
