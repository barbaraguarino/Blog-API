package br.com.blog.core.exceptions.domain;

import lombok.Getter;

@Getter
public abstract class InfrastructureException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    protected InfrastructureException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause); // Preserva o erro original (ex: do Auth0) para os logs!
        this.messageKey = messageKey;
        this.args = args;
    }
}
