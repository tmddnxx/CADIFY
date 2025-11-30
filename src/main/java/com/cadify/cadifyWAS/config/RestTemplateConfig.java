package com.cadify.cadifyWAS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean // This annotation tells Spring to create and manage an instance of RestTemplate
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
