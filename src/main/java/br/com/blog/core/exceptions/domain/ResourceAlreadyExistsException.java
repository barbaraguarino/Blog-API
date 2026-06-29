package br.com.blog.core.exceptions.domain;

public class ResourceAlreadyExistsException extends DomainException {
    public ResourceAlreadyExistsException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
