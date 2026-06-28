package br.com.blog.core.exceptions.domain;

public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
