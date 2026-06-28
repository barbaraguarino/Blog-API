package br.com.blog.modules.user.controllers.registration;

import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.registration.RegisterUserRequest;
import br.com.blog.modules.user.dtos.shared.UserProfileResponse;
import br.com.blog.modules.user.mappers.UserMapper;
import br.com.blog.modules.user.services.registration.UserRegistrationService;
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

    private final UserRegistrationService userRegistrationService;
    private final UserMapper userMapper;

    @PostMapping()
    public ResponseEntity<UserProfileResponse> register(@RequestBody @Valid RegisterUserRequest dto){

        var response = userMapper.toResponseDTO(userRegistrationService.registerUser(dto));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/google")
    public ResponseEntity<UserProfileResponse> registerWithGoogle(@RequestBody @Valid GoogleAuthRequest dto) {

        var response = userMapper.toResponseDTO(userRegistrationService.registerWithGoogle(dto));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
