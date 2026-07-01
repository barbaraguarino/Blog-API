package br.com.blog.modules.user.dtos.auth;

public record GoogleUserInfoDTO(
        String googleId,
        String email,
        String name
){}
