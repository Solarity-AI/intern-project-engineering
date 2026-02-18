package com.example.productreview.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "<h1>Solarity AI Backend is Running!</h1>" +
               "<p>Check out the API: <a href='/api/v1/products'>/api/v1/products</a></p>";
    }
}
