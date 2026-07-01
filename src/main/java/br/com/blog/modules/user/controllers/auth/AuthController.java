package br.com.blog.modules.user.controllers.auth;

import br.com.blog.modules.user.dtos.auth.AuthResult;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.auth.LoginRequest;
import br.com.blog.modules.user.dtos.shared.UserProfileResponse;
import br.com.blog.modules.user.services.auth.AuthenticateGoogleUserService;
import br.com.blog.modules.user.services.auth.AuthenticateLocalUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateLocalUserService authenticateLocalUserService;
    private final AuthenticateGoogleUserService authenticateGoogleUserService;

    @Value("${api.security.token.name}")
    private String cookieName;

    @PostMapping("/login")
    public ResponseEntity<UserProfileResponse> login(@RequestBody @Valid LoginRequest dto) {

        AuthResult result = authenticateLocalUserService.execute(dto);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, getResponseCookie(result.token()).toString())
                .body(result.userProfile());
    }

    @PostMapping("/login/google")
    public ResponseEntity<UserProfileResponse> loginWithGoogle(@RequestBody @Valid GoogleAuthRequest dto) {

        AuthResult result = authenticateGoogleUserService.execute(dto);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, getResponseCookie(result.token()).toString())
                .body(result.userProfile());
    }

    private ResponseCookie getResponseCookie(String value) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(false) // Mude para true em Produção
                .sameSite("Strict")
                .path("/")
                .maxAge(86400)
                .build();
    }
}
