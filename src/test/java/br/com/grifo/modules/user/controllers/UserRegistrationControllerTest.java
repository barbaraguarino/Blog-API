package br.com.grifo.modules.user.controllers;


import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.services.UserRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRegistrationService userRegistrationService;

    @Test
    @DisplayName("Deve retornar 201 (Created) e o DTO do usuário ao enviar payload valido")
    void shouldReturn201WhenPayloadIsValid() throws Exception {
        UserRegistrationDTO requestDTO = new UserRegistrationDTO(
                "Bárbara Guarino", "barbara@grifo.com", "SenhaForte@123"
        );

        UserResponseDTO responseDTO = new UserResponseDTO(
                UUID.randomUUID(), "Bárbara Guarino", "barbara@grifo.com",
                "barbara_guarino_1234", "READER", true, true, false, LocalDateTime.now()
        );

        when(userRegistrationService.registerUser(any(UserRegistrationDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nickname").value("barbara_guarino_1234"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Deve retornar 409 (Conflict) ao tentar cadastrar e-mail ja existente")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        UserRegistrationDTO requestDTO = new UserRegistrationDTO(
                "Bárbara Guarino", "existente@grifo.com", "SenhaForte@123"
        );

        when(userRegistrationService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new BusinessException("error.user.already_exists", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) ao enviar payload com senha fraca")
    void shouldReturn400WhenPasswordIsWeak() throws Exception {
        UserRegistrationDTO requestDTO = new UserRegistrationDTO(
                "Bárbara Guarino", "barbara@grifo.com", "fraca123"
        );

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }


}