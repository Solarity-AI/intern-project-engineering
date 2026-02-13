package com.solarityai.productreview.config;

import com.solarityai.backendfw.exception.NotFoundException;
import com.solarityai.backendfw.foundation.model.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * App-level exception handler that guarantees all errors return
 * {@link ApiErrorResponse} — the mobile client's expected contract.
 *
 * Takes highest precedence so the response shape is always consistent,
 * even if framework handler beans fail to initialise.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // ── Validation (422) ────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(fe -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("field", fe.getField());
                    m.put("message", fe.getDefaultMessage());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(422)
                .body(error("VALIDATION_ERROR", "Validation failed", request, details));
    }

    // ── Bad request (400) ───────────────────────────────────────────

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            Exception ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("BAD_REQUEST", ex.getMessage(), request, null));
    }

    // ── Not found (404) ─────────────────────────────────────────────

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            NotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("NOT_FOUND", ex.getMessage(), request, null));
    }

    // ── Forbidden (403) ─────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            AccessDeniedException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error("FORBIDDEN", "Access denied", request, null));
    }

    // ── Catch-all (500) ─────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("INTERNAL_ERROR", "An unexpected error occurred", request, null));
    }

    // ── Helper ──────────────────────────────────────────────────────

    private static ApiErrorResponse error(String code, String message,
                                          HttpServletRequest request,
                                          Map<String, Object> details) {
        return ApiErrorResponse.builder()
                .errorCode(code)
                .message(message)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .details(details)
                .build();
    }
}
