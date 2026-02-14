package com.example.productreview.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Review API")
                        .version("1.0.0")
                        .description("REST API for the Product Review Application. "
                                + "Browse products, submit reviews, manage wishlists, "
                                + "and interact with an AI assistant for review analysis.")
                        .contact(new Contact()
                                .name("Solarity Engineering Team")
                                .email("engineering@solarity.com")));
    }

    @Bean
    public OperationCustomizer globalHeaderCustomizer() {
        Parameter userIdHeader = new Parameter()
                .in("header")
                .name("X-User-ID")
                .description("Device-generated UUID identifying the user. "
                        + "Required for all user-scoped endpoints (wishlist, notifications, review voting).")
                .required(false)
                .schema(new io.swagger.v3.oas.models.media.StringSchema());

        return (operation, handlerMethod) -> {
            boolean alreadyHas = operation.getParameters() != null
                    && operation.getParameters().stream()
                            .anyMatch(p -> "X-User-ID".equals(p.getName()));
            if (!alreadyHas) {
                operation.addParametersItem(userIdHeader);
            }
            return operation;
        };
    }
}
