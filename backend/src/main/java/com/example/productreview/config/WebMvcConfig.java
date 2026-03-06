package com.example.productreview.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthenticatedUserIdArgumentResolver authenticatedUserIdArgumentResolver;

    public WebMvcConfig(AuthenticatedUserIdArgumentResolver authenticatedUserIdArgumentResolver) {
        this.authenticatedUserIdArgumentResolver = authenticatedUserIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUserIdArgumentResolver);
    }
}
