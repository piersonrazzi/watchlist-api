package dev.razzi.watchlist.web;

import dev.razzi.watchlist.service.InvalidRequestException;
import dev.razzi.watchlist.service.NotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Consistent error responses using ProblemDetail (RFC 9457,
 * Content-Type "application/problem+json"), built into Spring 6 / Boot 3.
 *
 * Extending ResponseEntityExceptionHandler means every standard Spring MVC
 * error (malformed JSON, wrong HTTP method, unsupported media type...) already
 * comes back as a ProblemDetail. We only add handlers for our own exceptions,
 * plus a per-field breakdown for validation failures.
 *
 * In the Java 11 / Boot 2.7 version this class built its own Map-based JSON
 * and handled HttpMessageNotReadableException by hand.
 *
 * Example body:
 * {
 *   "type": "about:blank", "title": "Resource not found", "status": 404,
 *   "detail": "Watchlist 42 not found", "instance": "/api/watchlists/42"
 * }
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail invalidRequest(InvalidRequestException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }

    /** @Valid failures: keep Spring's ProblemDetail, add every bad field. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ProblemDetail body = ex.getBody();
        body.setTitle("Validation failed");
        // Custom properties are serialized as top-level JSON fields.
        body.setProperty("fieldErrors", fieldErrors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
