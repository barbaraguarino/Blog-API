package br.com.blog.core.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(

    LocalDateTime timestamp,
    Integer status,
    String error,
    String message,
    String path,
    Map<String, String> validationErrors
){
    public static ErrorResponseDTO create(Integer status,
                                          String error,
                                          String message,
                                          String path,
                                          Map<String, String> validationErrors) {
        return new ErrorResponseDTO(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                validationErrors);
    }

    public static ErrorResponseDTO create(Integer status,
                                          String error,
                                          String message,
                                          String path) {
        return new ErrorResponseDTO(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null
        );
    }
}
