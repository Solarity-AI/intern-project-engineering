package com.example.productreview.service;

import com.example.productreview.BaseIntegrationTest;
import com.example.productreview.dto.ReviewDTO;
import com.example.productreview.model.Product;
import com.example.productreview.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Concurrency regression tests for review submission statistics.
 *
 * <p>These tests guard against re-introduction of the read-modify-write race
 * condition that was fixed by {@code ProductRepository.updateProductStatsAtomic}.
 *
 * <p><b>H2 vs PostgreSQL note:</b> H2's default transaction isolation is
 * READ_COMMITTED, which is the same as PostgreSQL's default. However, H2 uses
 * a single in-process JVM lock for serialization of concurrent writes and does
 * not exhibit the same multi-process MVCC behaviour as PostgreSQL. These tests
 * therefore provide a good functional regression guard, but a full concurrency
 * stress test should also be run against PostgreSQL in a staging environment to
 * validate behaviour under true multi-process I/O contention.
 */
public class ReviewConcurrencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Verifies that {@code review_count} is consistent after 10 concurrent
     * submissions against a product that already has seeded reviews.
     */
    @Test
    void concurrentReviewSubmissions_ShouldProduceCorrectReviewCount() throws Exception {
        Long productId = 1L;
        int threadCount = 10;

        Product before = productRepository.findById(productId).orElseThrow();
        int initialCount = before.getReviewCount();

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                ReviewDTO dto = new ReviewDTO();
                dto.setReviewerName("ConcurrentUser" + index);
                dto.setComment("Concurrent review number " + index + " for testing");
                dto.setRating((index % 5) + 1);

                readyLatch.countDown();
                startLatch.await();

                return mockMvc.perform(post("/api/v1/products/" + productId + "/reviews")
                                .with(clerkAuth("concurrent-reviewer-" + index))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getStatus();
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<Integer> future : futures) {
            future.get();
        }
        executor.shutdown();

        Product after = productRepository.findById(productId).orElseThrow();
        assertEquals(initialCount + threadCount, after.getReviewCount(),
                "Review count should equal initial + " + threadCount + " concurrent submissions");
    }

    /**
     * Verifies that both {@code review_count} and {@code average_rating} are
     * consistent after 10 concurrent submissions against a product that starts
     * with zero reviews. Using a fresh product ensures the expected average is
     * fully deterministic: ratings [1,2,3,4,5,1,2,3,4,5] sum to 30, giving an
     * average of 3.0.
     */
    @Test
    void concurrentReviewSubmissions_ShouldProduceCorrectAverageRating() throws Exception {
        Product freshProduct = new Product();
        freshProduct.setName("Concurrency Test Product");
        freshProduct.setDescription("Dedicated product for concurrent average-rating assertions");
        freshProduct.setPrice(1.0);
        freshProduct.setCategories(Set.of("Electronics"));
        freshProduct.setReviewCount(0);
        freshProduct.setAverageRating(0.0);
        Long productId = productRepository.save(freshProduct).getId();

        int threadCount = 10;
        // Ratings per thread: 1,2,3,4,5,1,2,3,4,5  →  sum=30, expected avg=3.0
        double expectedAverage = 3.0;

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                ReviewDTO dto = new ReviewDTO();
                dto.setReviewerName("RatingUser" + index);
                dto.setComment("Average rating concurrency test review " + index);
                dto.setRating((index % 5) + 1);

                readyLatch.countDown();
                startLatch.await();

                return mockMvc.perform(post("/api/v1/products/" + productId + "/reviews")
                                .with(clerkAuth("rating-tester-" + index))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getStatus();
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<Integer> future : futures) {
            future.get();
        }
        executor.shutdown();

        Product after = productRepository.findById(productId).orElseThrow();
        assertEquals(threadCount, after.getReviewCount(),
                "Review count should equal the number of concurrent submissions");
        assertEquals(expectedAverage, after.getAverageRating(), 0.1,
                "Average rating should be 3.0 (ratings 1-5 twice, sum=30, count=10)");
    }
}
