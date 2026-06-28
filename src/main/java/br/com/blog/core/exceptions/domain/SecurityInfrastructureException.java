package br.com.blog.core.exceptions.domain;

public class SecurityInfrastructureException extends InfrastructureException {
    public SecurityInfrastructureException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause, args);
    }
}
