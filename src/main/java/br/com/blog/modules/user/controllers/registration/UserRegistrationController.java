package br.com.blog.modules.user.controllers.registration;

import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.registration.RegisterUserRequest;
import br.com.blog.modules.user.dtos.shared.UserProfileResponse;
import br.com.blog.modules.user.services.registration.RegisterGoogleUserService;
import br.com.blog.modules.user.services.registration.RegisterLocalUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/register")
@RequiredArgsConstructor
public class UserRegistrationController {

    private final RegisterLocalUserService registerLocalUserService;
    private final RegisterGoogleUserService registerGoogleUserService;

    @PostMapping()
    public ResponseEntity<UserProfileResponse> register(@RequestBody @Valid RegisterUserRequest dto) {

        UserProfileResponse response = registerLocalUserService.execute(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/google")
    public ResponseEntity<UserProfileResponse> registerWithGoogle(@RequestBody @Valid GoogleAuthRequest dto) {

        UserProfileResponse response = registerGoogleUserService.execute(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
