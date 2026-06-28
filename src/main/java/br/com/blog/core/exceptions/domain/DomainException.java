package br.com.blog.core.exceptions.domain;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

  private final String messageKey;
  private final Object[] args;

  protected DomainException(String messageKey, Object... args) {
    super(messageKey);
    this.messageKey = messageKey;
    this.args = args;
  }
}