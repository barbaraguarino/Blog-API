package br.com.blog.core.exceptions.domain;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
