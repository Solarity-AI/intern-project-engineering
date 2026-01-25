package com.solarityai.productreview.service.impl;

import com.solarityai.productreview.entity.ProductEntity;
import com.solarityai.productreview.entity.ReviewEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIService {

    @Value("${openai.api.key:test-key}")
    private String openAiApiKey;

    private boolean isTestMode() {
        return openAiApiKey == null || openAiApiKey.equals("test-key") || openAiApiKey.equals("invalid");
    }

    @Cacheable(value = "aiSummaries", key = "#product.id")
    public String generateReviewSummary(ProductEntity product) {
        if (isTestMode()) {
            return generateMockSummary(product);
        }

        // In a real implementation, this would call OpenAI API
        // For now, return mock summary
        return generateMockSummary(product);
    }

    public String chatAboutProduct(ProductEntity product, List<ReviewEntity> reviews, String question) {
        if (isTestMode()) {
            return generateMockChatResponse(product, reviews, question);
        }

        // In a real implementation, this would call OpenAI API
        // For now, return mock response
        return generateMockChatResponse(product, reviews, question);
    }

    private String generateMockSummary(ProductEntity product) {
        int reviewCount = product.getReviewCount();
        double avgRating = product.getAverageRating();

        if (reviewCount == 0) {
            return "No reviews available yet for this product.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Based on %d reviews with an average rating of %.1f stars, ",
                reviewCount, avgRating));

        if (avgRating >= 4.5) {
            summary.append("customers are extremely satisfied with this product. ");
        } else if (avgRating >= 4.0) {
            summary.append("customers generally have positive experiences. ");
        } else if (avgRating >= 3.0) {
            summary.append("customers have mixed opinions about this product. ");
        } else {
            summary.append("customers report some concerns. ");
        }

        summary.append("Common themes include product quality, value for money, and overall performance.");

        return summary.toString();
    }

    private String generateMockChatResponse(ProductEntity product, List<ReviewEntity> reviews, String question) {
        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("recommend")) {
            if (product.getAverageRating() >= 4.0) {
                return String.format("Yes, I would recommend the %s. With an average rating of %.1f stars " +
                        "from %d reviews, most customers are satisfied with their purchase.",
                        product.getName(), product.getAverageRating(), product.getReviewCount());
            } else {
                return String.format("Based on the reviews, the %s has an average rating of %.1f stars " +
                        "from %d reviews. You might want to read the detailed reviews to make an informed decision.",
                        product.getName(), product.getAverageRating(), product.getReviewCount());
            }
        }

        if (lowerQuestion.contains("price") || lowerQuestion.contains("worth")) {
            return String.format("The %s is priced at %s. Based on customer reviews with an average rating of %.1f, " +
                    "most customers find it provides good value for the money.",
                    product.getName(), product.getPrice(), product.getAverageRating());
        }

        if (lowerQuestion.contains("quality")) {
            if (product.getAverageRating() >= 4.0) {
                return "Customers generally report high quality and satisfaction with this product.";
            } else {
                return "Reviews indicate mixed experiences with product quality. Check detailed reviews for more specific feedback.";
            }
        }

        if (lowerQuestion.contains("problem") || lowerQuestion.contains("issue") || lowerQuestion.contains("complaint")) {
            if (product.getAverageRating() < 4.0) {
                return "Some customers have reported concerns. I recommend reading through the lower-rated reviews " +
                        "to understand potential issues before making a purchase decision.";
            } else {
                return "Most customers are satisfied. While there may be some minor issues mentioned in reviews, " +
                        "the overall feedback is positive.";
            }
        }

        // Default response
        return String.format("The %s has received %d reviews with an average rating of %.1f stars. " +
                "For specific details about your question, I recommend reviewing the customer feedback section.",
                product.getName(), product.getReviewCount(), product.getAverageRating());
    }
}
