package br.com.blog.core.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

    LocalDateTime timestamp,
    Integer status,
    String error,
    String message,
    String path,
    Map<String, String> validationErrors
){
    public static ErrorResponse create(Integer status,
                                       String error,
                                       String message,
                                       String path,
                                       Map<String, String> validationErrors) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                validationErrors);
    }

    public static ErrorResponse create(Integer status,
                                       String error,
                                       String message,
                                       String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null
        );
    }
}
