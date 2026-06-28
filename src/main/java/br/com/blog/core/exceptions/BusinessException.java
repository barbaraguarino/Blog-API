package br.com.blog.core.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException{

    private final HttpStatus httpStatus;
    private final String messageKey;
    private final Object[] args;

    public BusinessException(String messageKey, HttpStatus httpStatus, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
        this.args = args;
    }

    public BusinessException(String messageKey, Object... args) {
        this(messageKey, HttpStatus.BAD_REQUEST, args);
    }
}
