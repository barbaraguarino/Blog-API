package br.com.grifo.core.exceptions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String URI_ERROR_500 = "/dummy/error-500";
    private static final String URI_ERROR_400 = "/dummy/error-400";
    private static final String URI_ERROR_BUSINESS = "/dummy/error-business";

    private MockMvc mockMvc;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {

        when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    return switch (key) {
                        case "error.server.internal" -> "Ocorreu um erro interno inesperado no servidor. Contate o suporte.";
                        case "error.validation.generic" -> "Erro de validação nos dados fornecidos.";
                        case "error.user.already_exists" -> "Erro de regra de negócio: error.user.already_exists";
                        default -> key;
                    };
                });

        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    @DisplayName("Deve retornar HTTP 500 e mensagem genérica ao ocorrer Exception não mapeada")
    void handleAllUncaughtExceptionAndReturns500() throws Exception {

        mockMvc.perform(get(URI_ERROR_500))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno inesperado no servidor. Contate o suporte."))
                .andExpect(jsonPath("$.path").value(URI_ERROR_500))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 e lista de erros ao falhar na validação do DTO")
    void handleValidationExceptionAndReturns400WithErrors() throws Exception {

        String invalidJson = "{ \"email\": \"email-invalido\" }";

        mockMvc.perform(post(URI_ERROR_400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Erro de validação nos dados fornecidos."))
                .andExpect(jsonPath("$.path").value(URI_ERROR_400))
                .andExpect(jsonPath("$.validationErrors.email").value("E-mail com formato inválido"));
    }

    @Test
    @DisplayName("Deve retornar o HTTP Status correspondente ao lançar BusinessException")
    void handleBusinessExceptionAndReturnsCustomStatus() throws Exception {

        mockMvc.perform(get(URI_ERROR_BUSINESS))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Erro de regra de negócio: error.user.already_exists"))
                .andExpect(jsonPath("$.path").value(URI_ERROR_BUSINESS));
    }

    @RestController
    static class DummyController {

        @GetMapping(URI_ERROR_500)
        public void throwException() throws Exception {

            throw new Exception("Erro grotesco no banco de dados que não deve vazar para o usuário");
        }

        @PostMapping(URI_ERROR_400)
        public void throwValidationException(@Valid @RequestBody DummyDto ignored) {}

        @GetMapping(URI_ERROR_BUSINESS)
        public void throwBusinessException() {

            throw new BusinessException("error.user.already_exists", HttpStatus.CONFLICT);
        }
    }

    record DummyDto(
            @Email(message = "E-mail com formato inválido")
            String email
    ){}

}