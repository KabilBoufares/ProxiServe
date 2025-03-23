package com.proxiserve.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.LOCKED) // 423 Locked ou 403 si tu préfères
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 423,
                        "error", "Compte verrouillé",
                        "message", ex.getMessage(),
                        "path", request.getRequestURI()
                ));
    }
}
