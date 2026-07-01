package br.com.blog.modules.user.application.dtos.auth.internal;

public record GoogleUserInfoDTO(
        String googleId,
        String email,
        String name
){}
