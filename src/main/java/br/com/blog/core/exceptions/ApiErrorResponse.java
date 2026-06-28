package br.com.blog.core.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;

}
