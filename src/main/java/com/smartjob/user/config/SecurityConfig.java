package com.smartjob.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for password encryption.
 * Uses BCrypt as the hashing algorithm.
 */
@Configuration
public class SecurityConfig {
    /**
     * Bean for encrypting and validating passwords using BCrypt.
     *
     * @return an instance of BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
