package br.com.blog.modules.user.dtos.auth;

public record GoogleUserInfo(
        String googleId,
        String email,
        String name
){}
