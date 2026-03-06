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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReviewConcurrencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

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
}
