package br.com.fiap.fordradar.exceptions;

import br.com.fiap.fordradar.dtos.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("[AUDIT] Resource not found - path: {}", request.getRequestURI());
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRuleException(
            BusinessRuleException ex, HttpServletRequest request) {
        log.warn("[AUDIT] Business rule violation - path: {}", request.getRequestURI());
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Unprocessable Entity")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("[AUDIT] Validation error - path: {}", request.getRequestURI());
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce("", (a, b) -> a + b + "; ");
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message(errorMessage)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Credenciais inválidas.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("[AUDIT] Access denied - path: {}", request.getRequestURI());
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Seu perfil não tem permissão para este recurso.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Corpo da requisição inválido.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllUncaughtException(
            Exception ex, HttpServletRequest request) {
        log.error("[AUDIT] Unexpected error - type: {} - path: {}",
                ex.getClass().getSimpleName(), request.getRequestURI());
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocorreu um erro interno inesperado. Tente novamente mais tarde.")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}