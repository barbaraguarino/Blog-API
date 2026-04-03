package br.com.grifo.modules.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ADMIN("ROLE_ADMIN"),
    MODERATOR("ROLE_MODERATOR"),
    AUTHOR("ROLE_AUTHOR"),
    READER("ROLE_READER");

    private final String role;
}
