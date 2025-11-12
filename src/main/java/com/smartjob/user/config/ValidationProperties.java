package com.smartjob.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration properties for business validations.
 * Regex patterns are loaded from application.yaml and are configurable.
 */
@Configuration
@ConfigurationProperties(prefix = "validation")
@Getter
@Setter
public class ValidationProperties {

    private ValidationRule email = new ValidationRule();
    private ValidationRule password = new ValidationRule();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationRule {
        private String pattern;
        private String message;
    }
}
