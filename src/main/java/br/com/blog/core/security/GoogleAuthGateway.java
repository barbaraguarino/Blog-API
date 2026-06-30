package br.com.blog.core.security;

import br.com.blog.modules.user.dtos.auth.GoogleUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthGateway {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public GoogleUserInfo extractUserInfo(String token) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(token);

            if (idToken == null) {
                log.warn("Token do Google recebido é nulo ou inválido.");
                throw new BadCredentialsException("error.auth.google_token_invalid");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            return new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    payload.get("name") != null ? payload.get("name").toString() : ""
            );

        } catch (BadCredentialsException e) {
            throw e; // Repassa a exceção de negócio
        } catch (Exception e) {
            log.error("Erro interno ao tentar validar token do Google: {}", e.getMessage());
            throw new BadCredentialsException("error.auth.google_token_invalid");
        }
    }
}
