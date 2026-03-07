package com.example.productreview.config;

import com.example.productreview.model.UserMapping;
import com.example.productreview.service.UserMappingService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AuthenticatedUserIdArgumentResolverTest {

    private final UserMappingService userMappingService = mock(UserMappingService.class);
    private final AuthenticatedUserIdArgumentResolver resolver =
            new AuthenticatedUserIdArgumentResolver(userMappingService);

    @Test
    void supportsParameter_WithAnnotatedString_ShouldReturnTrue() throws Exception {
        assertTrue(resolver.supportsParameter(methodParameter("annotatedParameter")));
    }

    @Test
    void supportsParameter_WithoutAnnotation_ShouldReturnFalse() throws Exception {
        assertFalse(resolver.supportsParameter(methodParameter("plainParameter")));
    }

    @Test
    void resolveArgument_WithAuthenticatedUserAttribute_ShouldReturnUserId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
                ClerkAuthenticationFilter.AUTHENTICATED_INTERNAL_USER_ID_ATTRIBUTE,
                "314");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        Object resolved = resolver.resolveArgument(
                methodParameter("annotatedParameter"),
                null,
                webRequest,
                null);

        assertEquals("314", resolved);
        verifyNoInteractions(userMappingService);
    }

    @Test
    void resolveArgument_WithAuthenticatedClerkUserAttribute_ShouldResolveAndCacheInternalUserId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
                ClerkAuthenticationFilter.AUTHENTICATED_CLERK_USER_ID_ATTRIBUTE,
                "clerk-user-22");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        UserMapping userMapping = new UserMapping("clerk-user-22");
        userMapping.setInternalUserId(22L);
        when(userMappingService.getOrCreateByClerkUserId("clerk-user-22")).thenReturn(userMapping);

        Object resolved = resolver.resolveArgument(
                methodParameter("annotatedParameter"),
                null,
                webRequest,
                null);

        assertEquals("22", resolved);
        assertEquals(
                "22",
                webRequest.getAttribute(
                        ClerkAuthenticationFilter.AUTHENTICATED_INTERNAL_USER_ID_ATTRIBUTE,
                        RequestAttributes.SCOPE_REQUEST));
    }

    @Test
    void resolveArgument_WithoutAuthenticatedUserAttribute_ShouldThrow() throws Exception {
        ServletWebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());

        AuthenticatedUserContextMissingException exception = assertThrows(
                AuthenticatedUserContextMissingException.class,
                () -> resolver.resolveArgument(methodParameter("annotatedParameter"), null, webRequest, null));

        assertEquals("Authenticated user context is missing", exception.getMessage());
    }

    private MethodParameter methodParameter(String methodName) throws Exception {
        Method method = ResolverFixture.class.getDeclaredMethod(methodName, String.class);
        return new MethodParameter(method, 0);
    }

    private static final class ResolverFixture {
        @SuppressWarnings("unused")
        void annotatedParameter(@AuthenticatedUserId String userId) {
        }

        @SuppressWarnings("unused")
        void plainParameter(String userId) {
        }
    }
}
