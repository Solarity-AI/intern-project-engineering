package com.example.productreview.service;

import com.example.productreview.model.Product;
import com.example.productreview.model.Review;
import com.example.productreview.repository.ProductRepository;
import com.example.productreview.repository.ReviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    public DataInitializer(ProductRepository productRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            List<Product> products = new ArrayList<>();

            // Electronics (Phones)
            products.add(createProduct("iPhone 15 Pro", "The latest iPhone with A17 Pro chip and Titanium design.", new HashSet<>(Arrays.asList("Electronics", "Smartphones")), 999.99, "https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Samsung Galaxy S24 Ultra", "AI-powered smartphone with S-Pen.", new HashSet<>(Arrays.asList("Electronics", "Smartphones")), 1199.99, "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Google Pixel 8 Pro", "The best of Google AI and camera.", new HashSet<>(Arrays.asList("Electronics", "Smartphones")), 899.99, "https://images.unsplash.com/photo-1696446701796-da61225697cc?auto=format&fit=crop&q=80&w=800"));

            // Laptops
            products.add(createProduct("MacBook Air M2", "Strikingly thin design and incredible speed.", new HashSet<>(Arrays.asList("Laptops", "Electronics")), 1099.00, "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Dell XPS 13", "Compact and powerful ultrabook.", new HashSet<>(Arrays.asList("Laptops", "Electronics")), 1299.00, "https://images.unsplash.com/photo-1593642702821-c8da6771f0c6?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Asus ROG Zephyrus", "Gaming power in a slim chassis.", new HashSet<>(Arrays.asList("Laptops", "Gaming")), 1799.00, "https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&q=80&w=800"));

            // Tablets
            products.add(createProduct("iPad Pro 12.9", "The ultimate iPad experience with M2 chip.", new HashSet<>(Arrays.asList("Tablets", "Electronics")), 1099.00, "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Samsung Galaxy Tab S9", "Dynamic AMOLED 2X display for stunning visuals.", new HashSet<>(Arrays.asList("Tablets", "Electronics")), 799.99, "https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Microsoft Surface Pro 9", "Laptop power, tablet flexibility.", new HashSet<>(Arrays.asList("Tablets", "Laptops")), 999.99, "https://images.unsplash.com/photo-1542744094-3a31f272c490?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("iPad Air 5", "Light. Bright. Full of might.", new HashSet<>(Arrays.asList("Tablets", "Electronics")), 599.00, "https://images.unsplash.com/photo-1589739900243-4b52cd9b104e?auto=format&fit=crop&q=80&w=800"));

            // Wearables
            products.add(createProduct("Apple Watch Series 9", "Smarter, brighter, and more powerful.", new HashSet<>(Arrays.asList("Wearables", "Electronics")), 399.00, "https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Samsung Galaxy Watch 6", "Advanced sleep coaching and heart monitoring.", new HashSet<>(Arrays.asList("Wearables", "Electronics")), 299.00, "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?auto=format&fit=crop&q=80&w=800"));

            // Gaming
            products.add(createProduct("Razer DeathAdder V3", "Ultra-lightweight ergonomic esports mouse.", new HashSet<>(Arrays.asList("Gaming", "Accessories")), 149.99, "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Keychron Q1 Pro", "Custom mechanical keyboard with QMK/VIA support.", new HashSet<>(Arrays.asList("Gaming", "Accessories")), 199.00, "https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Alienware 34 Monitor", "Curved QD-OLED gaming monitor.", new HashSet<>(Arrays.asList("Gaming", "Electronics")), 899.00, "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("PS5 DualSense Controller", "Immersive haptic feedback and dynamic triggers.", new HashSet<>(Arrays.asList("Gaming", "Accessories")), 69.99, "https://images.unsplash.com/photo-1606318801954-d46d46d3360a?auto=format&fit=crop&q=80&w=800"));

            // Audio
            products.add(createProduct("Sony WH-1000XM5", "Industry-leading noise canceling headphones.", new HashSet<>(Arrays.asList("Audio", "Electronics")), 349.99, "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("AirPods Pro 2", "Adaptive Audio and Active Noise Cancellation.", new HashSet<>(Arrays.asList("Audio", "Electronics", "Accessories")), 249.00, "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("JBL Flip 6", "Bold sound for every adventure.", new HashSet<>(Arrays.asList("Audio", "Electronics")), 129.95, "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Sonos Era 100", "Next-gen acoustics and new levels of connectivity.", new HashSet<>(Arrays.asList("Audio", "Electronics")), 249.00, "https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&q=80&w=800"));

            // Accessories
            products.add(createProduct("Anker 737 Power Bank", "Ultra-powerful two-way charging.", new HashSet<>(Arrays.asList("Accessories", "Electronics")), 149.99, "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Logitech MX Master 3S", "Performance wireless mouse.", new HashSet<>(Arrays.asList("Accessories", "Electronics")), 99.99, "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Bellroy Tech Kit", "Organize your cables and accessories.", new HashSet<>(Arrays.asList("Accessories")), 59.00, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=800"));
            products.add(createProduct("Nomad Base One", "Premium MagSafe charger.", new HashSet<>(Arrays.asList("Accessories", "Electronics")), 99.95, "https://images.unsplash.com/photo-1616348436168-de43ad0db179?auto=format&fit=crop&q=80&w=800"));

            List<Product> savedProducts = productRepository.saveAll(products);

            // Add random reviews to all products
            Random random = new Random();
            String[] names = {"Michael", "Sarah", "David", "Emma", "James", "Olivia", "Robert", "Sophia", "William", "Isabella"};
            String[] comments = {
                "Great product, highly recommended!",
                "Not bad, but a bit expensive.",
                "Fast delivery and good quality.",
                "I love the design.",
                "Performance is top notch.",
                "Battery drains a bit fast.",
                "Screen is beautiful.",
                "Worth every penny.",
                "Just okay.",
                "Exceeded my expectations."
            };

            for (Product p : savedProducts) {
                int reviewCount = random.nextInt(10) + 1; // 1 to 10 reviews per product
                for (int i = 0; i < reviewCount; i++) {
                    String name = names[random.nextInt(names.length)];
                    String comment = comments[random.nextInt(comments.length)];
                    int rating = random.nextInt(5) + 1; // 1-5
                    addReview(p, name, comment, rating);
                }
                updateProductStats(p);
            }

            // Add bulk reviews for iPhone 15 Pro (first product) for pagination testing
            Product iphone = savedProducts.get(0);
            for (int i = 0; i < 30; i++) {
                String name = names[random.nextInt(names.length)];
                String comment = comments[random.nextInt(comments.length)];
                int rating = random.nextInt(5) + 1;
                addReview(iphone, name, comment + " (Test Review " + (i + 1) + ")", rating);
            }
            updateProductStats(iphone);
        }
    }

    private Product createProduct(String name, String description, Set<String> categories, Double price, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setCategories(categories); // ✨ Updated
        p.setPrice(price);
        p.setImageUrl(imageUrl);
        return p;
    }

    private void addReview(Product product, String name, String comment, int rating) {
        Review review = new Review();
        review.setProduct(product);
        review.setReviewerName(name);
        review.setComment(comment);
        review.setRating(rating);
        review.setHelpfulCount(0);
        reviewRepository.save(review);
    }

    private void updateProductStats(Product product) {
        List<Object[]> results = reviewRepository.getReviewStats(product.getId());

        int count = 0;
        double average = 0.0;

        if (results != null && !results.isEmpty() && results.get(0) != null) {
            Object[] stats = results.get(0);
            count = stats.length > 0 && stats[0] != null ? ((Number) stats[0]).intValue() : 0;
            average = stats.length > 1 && stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
        }

        product.setReviewCount(count);
        product.setAverageRating(Math.round(average * 10.0) / 10.0);
        productRepository.save(product);
    }
}
