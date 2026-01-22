package com.productreview.backend.service.impl;

import com.productreview.backend.entity.Review;
import com.productreview.backend.service.AISummaryService;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AISummaryServiceImpl implements AISummaryService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max.tokens:500}")
    private Integer maxTokens;

    @Override
    @Cacheable(value = "aiSummaries", key = "#productId")
    public String generateReviewSummary(Long productId, String productName, List<Review> reviews) {
        if (openaiApiKey == null || openaiApiKey.isEmpty() || openaiApiKey.equals("your-api-key-here")) {
            return generateMockSummary(productName, reviews);
        }

        try {
            String reviewsText = reviews.stream()
                    .map(r -> String.format("Rating: %d/5 - %s", r.getRating(), r.getComment()))
                    .collect(Collectors.joining("\n"));

            String prompt = String.format(
                    "Analyze these customer reviews for the product '%s' and provide a brief, helpful summary " +
                    "(2-3 sentences) highlighting the main pros and cons mentioned by customers:\n\n%s",
                    productName, reviewsText
            );

            SimpleOpenAI openAI = SimpleOpenAI.builder()
                    .apiKey(openaiApiKey)
                    .build();

            var chatRequest = ChatRequest.builder()
                    .model(model)
                    .message(ChatMessage.UserMessage.of(prompt))
                    .maxCompletionTokens(maxTokens)
                    .build();

            var response = openAI.chatCompletions().create(chatRequest).join();
            return response.firstContent();

        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage());
            return generateMockSummary(productName, reviews);
        }
    }

    @Override
    public String chatWithReviews(Long productId, String question, List<Review> reviews) {
        if (openaiApiKey == null || openaiApiKey.isEmpty() || openaiApiKey.equals("your-api-key-here")) {
            return generateMockChatResponse(question, reviews);
        }

        try {
            String reviewsText = reviews.stream()
                    .map(r -> String.format("Rating: %d/5 - %s", r.getRating(), r.getComment()))
                    .collect(Collectors.joining("\n"));

            String prompt = String.format(
                    "Based on these customer reviews:\n\n%s\n\nAnswer the following question: %s\n\n" +
                    "Provide a helpful, concise answer based only on the information from the reviews.",
                    reviewsText, question
            );

            SimpleOpenAI openAI = SimpleOpenAI.builder()
                    .apiKey(openaiApiKey)
                    .build();

            var chatRequest = ChatRequest.builder()
                    .model(model)
                    .message(ChatMessage.UserMessage.of(prompt))
                    .maxCompletionTokens(maxTokens)
                    .build();

            var response = openAI.chatCompletions().create(chatRequest).join();
            return response.firstContent();

        } catch (Exception e) {
            log.error("Error calling OpenAI API for chat: {}", e.getMessage());
            return generateMockChatResponse(question, reviews);
        }
    }

    private String generateMockSummary(String productName, List<Review> reviews) {
        if (reviews.isEmpty()) {
            return "No reviews available for this product yet.";
        }

        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        long positiveCount = reviews.stream().filter(r -> r.getRating() >= 4).count();
        long negativeCount = reviews.stream().filter(r -> r.getRating() <= 2).count();

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Based on %d reviews, %s has an average rating of %.1f/5. ",
                reviews.size(), productName, avgRating));

        if (positiveCount > negativeCount) {
            summary.append("Customers generally praise the product's quality and value. ");
        } else if (negativeCount > positiveCount) {
            summary.append("Some customers have expressed concerns that should be considered. ");
        } else {
            summary.append("Opinions are mixed among customers. ");
        }

        summary.append("Check individual reviews for specific details.");

        return summary.toString();
    }

    private String generateMockChatResponse(String question, List<Review> reviews) {
        if (reviews.isEmpty()) {
            return "I don't have any reviews to analyze for this product yet. Please check back later when customers have left their feedback.";
        }

        String questionLower = question.toLowerCase();

        if (questionLower.contains("quality") || questionLower.contains("good")) {
            long highRatings = reviews.stream().filter(r -> r.getRating() >= 4).count();
            return String.format("Based on %d reviews, %d customers (%.0f%%) gave high ratings (4+ stars), " +
                    "suggesting overall satisfaction with the product quality.",
                    reviews.size(), highRatings, (highRatings * 100.0 / reviews.size()));
        }

        if (questionLower.contains("recommend") || questionLower.contains("buy")) {
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            if (avgRating >= 4.0) {
                return "Based on the reviews, this product is highly recommended by customers with an average rating of " +
                        String.format("%.1f/5.", avgRating);
            } else if (avgRating >= 3.0) {
                return "Reviews are mixed. Consider reading individual reviews to make an informed decision.";
            } else {
                return "Based on reviews, some customers have had concerns. Read the reviews carefully before purchasing.";
            }
        }

        if (questionLower.contains("problem") || questionLower.contains("issue") || questionLower.contains("bad")) {
            long lowRatings = reviews.stream().filter(r -> r.getRating() <= 2).count();
            if (lowRatings > 0) {
                return String.format("About %.0f%% of reviewers reported issues. Check the lower-rated reviews for specific concerns.",
                        (lowRatings * 100.0 / reviews.size()));
            }
            return "Most customers haven't reported significant issues with this product.";
        }

        return String.format("Based on %d customer reviews with an average rating of %.1f/5, " +
                "this product has received %s feedback overall. For specific details, please read individual reviews.",
                reviews.size(),
                reviews.stream().mapToInt(Review::getRating).average().orElse(0.0),
                reviews.stream().mapToInt(Review::getRating).average().orElse(0.0) >= 4 ? "positive" : "mixed");
    }
}
