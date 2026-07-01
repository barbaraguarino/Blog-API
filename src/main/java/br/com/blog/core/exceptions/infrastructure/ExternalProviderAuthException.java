package br.com.blog.core.exceptions.infrastructure;

public class ExternalProviderAuthException extends InfrastructureException {

    public ExternalProviderAuthException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause, args);
    }

    public ExternalProviderAuthException(String messageKey, Object... args) {
        super(messageKey, null, args);
    }
}
