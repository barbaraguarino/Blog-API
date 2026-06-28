package br.com.blog.modules.user.controllers.auth;

import br.com.blog.modules.user.dtos.auth.GoogleTokenDTO;
import br.com.blog.modules.user.dtos.auth.LoginRequestDTO;
import br.com.blog.modules.user.dtos.shared.UserResponseDTO;
import br.com.blog.modules.user.mappers.UserMapper;
import br.com.blog.modules.user.services.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final AuthService authService;
    private final UserMapper userMapper;
    private static final String TOKEN_NAME = "blog_token";

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {

        AuthService.AuthResult result = authService.authenticate(dto);

        var response = userMapper.toResponseDTO(result.user());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, getResponseCookie(result.token()).toString())
                .body(response);
    }

    @PostMapping("/login/google")
    public ResponseEntity<UserResponseDTO> loginWithGoogle(@RequestBody @Valid GoogleTokenDTO dto) {

        AuthService.AuthResult result = authService.authenticateWithGoogle(dto);

        var response = userMapper.toResponseDTO(result.user());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, getResponseCookie(result.token()).toString())
                .body(response);
    }

    private ResponseCookie getResponseCookie(String value) {

        return ResponseCookie.from(TOKEN_NAME, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(86400)
                .build();
    }
}
