package com.productreview.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductReviewApplication.class, args);
    }
}
