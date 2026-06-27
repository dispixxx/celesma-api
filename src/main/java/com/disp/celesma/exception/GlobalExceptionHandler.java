package com.disp.celesma.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("Файл слишком большой. Максимальный размер: 5MB");
    }

    @ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        log.error("error:", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.error("error:", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Неверный логин или пароль"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.error("error:", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalArgumentException ex) {
        log.error("error:", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.error("error:", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.error("error:", ex);
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Внутренняя ошибка сервера"));
    }

    public record ErrorResponse(int status, String message) {
        public LocalDateTime timestamp() {
            return LocalDateTime.now();
        }
    }
}
