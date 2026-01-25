package com.solarityai.productreview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class ProductReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductReviewApplication.class, args);
    }
}
