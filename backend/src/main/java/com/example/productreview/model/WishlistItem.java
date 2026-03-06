package com.example.productreview.model;

import jakarta.persistence.*;

@Entity
@Table(name = "wishlist_items", indexes = {
    @Index(name = "idx_wishlist_user", columnList = "user_id"),
    @Index(name = "idx_wishlist_product", columnList = "product_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_wishlist_user_product", columnNames = {"user_id", "product_id"})
})
public class WishlistItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId; // Internal user ID resolved from Clerk mapping
    
    @Column(nullable = false)
    private Long productId;

    public WishlistItem() {}

    public WishlistItem(String userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
