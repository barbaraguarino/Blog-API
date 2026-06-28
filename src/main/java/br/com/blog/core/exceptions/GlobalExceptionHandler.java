package br.com.blog.core.exceptions;

import br.com.blog.core.exceptions.domain.BusinessRuleException;
import br.com.blog.core.exceptions.domain.InfrastructureException;
import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /*
     * EXCEÇÕES DO DOMÍNIO (REGRAS DE NEGÓCIO)
     */

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessRuleException exception, HttpServletRequest request) {
        String message = getTranslatedMessage(exception.getMessageKey(), exception.getArgs(), request);
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, message, request, null);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception, HttpServletRequest request) {
        String message = getTranslatedMessage(exception.getMessageKey(), exception.getArgs(), request);
        return buildErrorResponse(HttpStatus.CONFLICT, message, request, null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundDomainException(ResourceNotFoundException exception, HttpServletRequest request) {
        String message = getTranslatedMessage(exception.getMessageKey(), exception.getArgs(), request);
        return buildErrorResponse(HttpStatus.NOT_FOUND, message, request, null);
    }

    /*
     * EXCEÇÕES DE INFRAESTRUTURA E FRAMEWORK (REGRAS DE NEGÓCIO)
     */

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ErrorResponseDTO> handleInfrastructureException(InfrastructureException exception, HttpServletRequest request) {
        String message = getTranslatedMessage(exception.getMessageKey(), exception.getArgs(), request);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(HttpServletRequest request) {
        String message = getTranslatedMessage("error.resource.not_found", null, request);
        return buildErrorResponse(HttpStatus.NOT_FOUND, message, request, null);
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<ErrorResponseDTO> handleDisabledOrLockedExceptions(HttpServletRequest request) {
        String message = getTranslatedMessage("error.auth.account_inactive", null, request);
        return buildErrorResponse(HttpStatus.FORBIDDEN, message, request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(HttpServletRequest request) {
        String message = getTranslatedMessage("error.auth.access_denied", null, request);
        return buildErrorResponse(HttpStatus.FORBIDDEN, message, request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpServletRequest request) {
        String message = getTranslatedMessage("error.http.method_not_supported", null, request);
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(HttpServletRequest request) {
        String message = getTranslatedMessage("error.auth.bad_credentials", null, request);
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, message, request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? getTranslatedMessage(fieldError.getDefaultMessage(), null, request)
                                : getTranslatedMessage("error.validation.field", null, request),
                        (existing, ignored) -> existing
                ));

        String message = getTranslatedMessage("error.validation.generic", null, request);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request, validationErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllUncaughtException(HttpServletRequest request) {
        String message = getTranslatedMessage("error.server.internal", null, request);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request, null);
    }

    /*
     * MÉTODOS UTILITÁRIOS
     */

    private String getTranslatedMessage(String key, Object[] args, HttpServletRequest request) {
        return messageSource.getMessage(key, args, key, request.getLocale());
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(
            HttpStatus status, String message, HttpServletRequest request, Map<String, String> validationErrors) {

        ErrorResponseDTO errorResponse;

        if (validationErrors != null && !validationErrors.isEmpty()) {
            errorResponse = ErrorResponseDTO.create(
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    request.getRequestURI(),
                    validationErrors
            );
        } else {
            errorResponse = ErrorResponseDTO.create(
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    request.getRequestURI()
            );
        }

        return ResponseEntity.status(status).body(errorResponse);
    }

}
