package com.example.productreview.config;

import com.example.productreview.model.UserMapping;
import com.example.productreview.service.UserMappingService;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticatedUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserMappingService userMappingService;

    public AuthenticatedUserIdArgumentResolver(UserMappingService userMappingService) {
        this.userMappingService = userMappingService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedUserId.class)
                && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Object internalUserId = webRequest.getAttribute(
                ClerkAuthenticationFilter.AUTHENTICATED_INTERNAL_USER_ID_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST);

        if (internalUserId instanceof String resolvedUserId && !resolvedUserId.isBlank()) {
            return resolvedUserId;
        }

        Object authenticatedClerkUserId = webRequest.getAttribute(
                ClerkAuthenticationFilter.AUTHENTICATED_CLERK_USER_ID_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST);

        if (!(authenticatedClerkUserId instanceof String clerkUserId) || clerkUserId.isBlank()) {
            throw new AuthenticatedUserContextMissingException("Authenticated user context is missing");
        }

        UserMapping userMapping = userMappingService.getOrCreateByClerkUserId(clerkUserId);
        String resolvedUserId = userMapping.getInternalUserId().toString();
        webRequest.setAttribute(
                ClerkAuthenticationFilter.AUTHENTICATED_INTERNAL_USER_ID_ATTRIBUTE,
                resolvedUserId,
                RequestAttributes.SCOPE_REQUEST);

        return resolvedUserId;
    }
}
