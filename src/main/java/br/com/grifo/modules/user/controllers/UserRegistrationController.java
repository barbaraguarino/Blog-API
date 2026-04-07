package br.com.grifo.modules.user.controllers;

import br.com.grifo.modules.user.dtos.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.services.UserRegistrationService;
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

    @PostMapping()
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid UserRegistrationDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userRegistrationService.registerUser(dto));
    }

    @PostMapping("/google")
    public ResponseEntity<UserResponseDTO> registerWithGoogle(@RequestBody @Valid GoogleTokenDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRegistrationService.registerWithGoogle(dto));
    }
}
