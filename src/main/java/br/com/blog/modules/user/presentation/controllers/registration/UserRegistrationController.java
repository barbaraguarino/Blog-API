package br.com.blog.modules.user.presentation.controllers.registration;

import br.com.blog.modules.user.application.dtos.auth.GoogleAuthRequestDTO;
import br.com.blog.modules.user.application.dtos.registration.RegisterUserRequestDTO;
import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;
import br.com.blog.modules.user.application.usecases.registration.RegisterGoogleUserService;
import br.com.blog.modules.user.application.usecases.registration.RegisterLocalUserService;
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
    public ResponseEntity<UserProfileResponseDTO> register(@RequestBody @Valid RegisterUserRequestDTO dto) {

        UserProfileResponseDTO response = registerLocalUserService.execute(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/google")
    public ResponseEntity<UserProfileResponseDTO> registerWithGoogle(@RequestBody @Valid GoogleAuthRequestDTO dto) {

        UserProfileResponseDTO response = registerGoogleUserService.execute(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
