package com.example.productreview.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class AppNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId; // UUID from frontend
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String message;
    
    private boolean isRead = false;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Optional: Link to a product
    private Long productId;

    public AppNotification() {}

    public AppNotification(String userId, String title, String message, Long productId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.productId = productId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
}
